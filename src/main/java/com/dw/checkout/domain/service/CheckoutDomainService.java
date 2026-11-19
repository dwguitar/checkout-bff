package com.dw.checkout.domain.service;



import com.dw.checkout.application.port.in.ProcessCheckoutUseCase;
import com.dw.checkout.application.port.in.RecoverStuckPaymentsUseCase;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.CheckoutCompletionPort;
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
import java.util.UUID;

public class CheckoutDomainService implements ProcessCheckoutUseCase, RecoverStuckPaymentsUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final CheckoutCompletionPort checkoutCompletionPort;

    public CheckoutDomainService(CartRepositoryPort cartRepositoryPort,
                                 PaymentGatewayPort paymentGatewayPort,
                                 CheckoutCompletionPort checkoutCompletionPort) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.checkoutCompletionPort = checkoutCompletionPort;
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

        String cardLastDigits = method.isCard() ? command.cardData().lastDigits() : null;
        UUID attemptId = UUID.randomUUID();
        if (!cartRepositoryPort.tryStartPayment(cart.getCartId(), method.name(), cardLastDigits, attemptId)) {
            Cart latestCart = cartRepositoryPort.findById(cart.getCartId()).orElse(cart);
            var error = latestCart.isApproved()
                    ? ErrorCatalog.CART_ALREADY_APPROVED
                    : ErrorCatalog.CHECKOUT_IN_PROGRESS;
            throw new CheckoutBusinessException(error, cart.getCartId());
        }
        cart = cartRepositoryPort.findById(cart.getCartId()).orElseThrow(
                () -> new CheckoutBusinessException(ErrorCatalog.CART_NOT_FOUND, command.cartId()));

        var result = paymentGatewayPort.charge(
                cart.getCartId(),
                cart.getPaymentAttemptId(),
                method,
                command.cardData(),
                cart.totalAmount()
        );

        var productSlugs = cart.getProducts().stream().map(Product::slug).toList();

        if (!result.approved()) {
            cart.decline();
            Cart completedCart = checkoutCompletionPort.saveAndEnqueue(cart, CheckoutPaymentEvent.of(
                    cart.getCartId(),
                    cart.getStatus(),
                    method.name(),
                    cart.totalAmount(),
                    cart.currency(),
                    productSlugs
            ));

            if (completedCart.isApproved()) {
                return completedCart;
            }

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
        return checkoutCompletionPort.saveAndEnqueue(cart, CheckoutPaymentEvent.of(
                cart.getCartId(),
                cart.getStatus(),
                method.name(),
                cart.totalAmount(),
                cart.currency(),
                productSlugs
        ));
    }

    @Override
    public void recoverBefore(Instant cutoff) {
        for (Cart cart : cartRepositoryPort.findStuckProcessingBefore(cutoff)) {
            if (!cartRepositoryPort.tryClaimStuckPaymentRecovery(cart.getCartId(), cutoff)) {
                continue;
            }
            cart = cartRepositoryPort.findById(cart.getCartId()).orElse(null);
            if (cart == null) {
                continue;
            }
            if (cart.getPaymentAttemptMethod() == null || cart.getPaymentProcessingAt() == null
                    || cart.getPaymentAttemptId() == null) {
                continue;
            }
            PaymentMethod method = new PaymentMethod(cart.getPaymentAttemptMethod());
            var attempt = new PaymentGatewayPort.PaymentAttempt(
                    cart.getCartId(), cart.getPaymentAttemptId(), method.name(), cart.getPaymentAttemptCardLastDigits(),
                    cart.totalAmount(), cart.currency());
            var result = paymentGatewayPort.findPaymentStatus(attempt);
            if (result.isEmpty()) {
                continue;
            }

            var paymentResult = result.get();
            if (paymentResult.approved()) {
                cart.approve(new PaymentReceipt(
                        method.name(), method.isCard() ? cart.getPaymentAttemptCardLastDigits() : null,
                        method.isPix() ? paymentResult.pixCode() : null,
                        cart.totalAmount(), cart.currency(), Instant.now()));
            } else {
                cart.decline();
            }

            checkoutCompletionPort.saveAndEnqueue(cart, CheckoutPaymentEvent.of(
                    cart.getCartId(), cart.getStatus(), method.name(), cart.totalAmount(),
                    cart.currency(), cart.getProducts().stream().map(Product::slug).toList()));
        }
    }
}
