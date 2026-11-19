package com.dw.checkout.domain.service;



import com.dw.checkout.application.port.in.ProcessCheckoutUseCase;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.PaymentEventPublisherPort;
import com.dw.checkout.application.port.out.PaymentGatewayPort;
import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.domain.exception.CheckoutBusinessException;
import com.dw.checkout.domain.exception.ErrorCatalog;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.CartStatus;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.PaymentReceipt;
import com.dw.checkout.domain.model.Product;

import java.time.Instant;

public class CheckoutDomainService implements ProcessCheckoutUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentEventPublisherPort paymentEventPublisherPort;

    public CheckoutDomainService(CartRepositoryPort cartRepositoryPort,
                                 PaymentGatewayPort paymentGatewayPort,
                                 PaymentEventPublisherPort paymentEventPublisherPort) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentEventPublisherPort = paymentEventPublisherPort;
    }

    @Override
    public Cart execute(CheckoutCommand command) {
        Cart cart = cartRepositoryPort.findById(command.cartId())
                .orElseThrow(() -> new CheckoutBusinessException(ErrorCatalog.CART_NOT_FOUND, command.cartId()));

        if (cart.isApproved()) {
            throw new CheckoutBusinessException(ErrorCatalog.CART_ALREADY_APPROVED, cart.getCartId());
        }

        if (cart.getStatus() == CartStatus.PROCESSING) {
            throw new CheckoutBusinessException(ErrorCatalog.CHECKOUT_IN_PROGRESS, cart.getCartId());
        }

        if (!cart.supportsPaymentMethod(command.paymentMethodName())) {
            throw new CheckoutBusinessException(ErrorCatalog.INVALID_PAYMENT_METHOD, cart.getCartId());
        }

        PaymentMethod method = new PaymentMethod(command.paymentMethodName());

        if (method.isCard() && command.cardData() == null) {
            throw new CheckoutBusinessException(ErrorCatalog.INVALID_PAYMENT_METHOD, cart.getCartId());
        }

        if (!cartRepositoryPort.tryStartPayment(cart.getCartId())) {
            Cart latestCart = cartRepositoryPort.findById(cart.getCartId()).orElse(cart);
            var error = latestCart.isApproved()
                    ? ErrorCatalog.CART_ALREADY_APPROVED
                    : ErrorCatalog.CHECKOUT_IN_PROGRESS;
            throw new CheckoutBusinessException(error, cart.getCartId());
        }

        var result = paymentGatewayPort.charge(
                cart.getCartId(),
                method,
                command.cardData(),
                cart.totalAmount()
        );

        var productSlugs = cart.getProducts().stream().map(Product::slug).toList();

        if (!result.approved()) {
            cart.decline();
            cartRepositoryPort.save(cart);

            paymentEventPublisherPort.publish(CheckoutPaymentEvent.of(
                    cart.getCartId(),
                    cart.getStatus(),
                    method.name(),
                    cart.totalAmount(),
                    cart.currency(),
                    productSlugs
            ));

            throw new CheckoutBusinessException(ErrorCatalog.PAYMENT_DECLINED, cart.getCartId());
        }

        String lastDigits = method.isCard() ? command.cardData().lastDigits() : null;
        String pixCode = method.isPix() ? result.pixCode() : null;

        PaymentReceipt receipt = new PaymentReceipt(
                method.name(),
                lastDigits,
                pixCode,
                cart.totalAmount(),
                cart.currency(),
                Instant.now()
        );

        cart.approve(receipt);
        Cart savedCart = cartRepositoryPort.save(cart);

        paymentEventPublisherPort.publish(CheckoutPaymentEvent.of(
                savedCart.getCartId(),
                savedCart.getStatus(),
                method.name(),
                savedCart.totalAmount(),
                savedCart.currency(),
                productSlugs
        ));

        return savedCart;
    }
}
