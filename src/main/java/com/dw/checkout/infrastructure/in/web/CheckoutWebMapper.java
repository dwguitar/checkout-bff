package com.dw.checkout.infrastructure.in.web;

import com.dw.checkout.application.port.in.ProcessCheckoutUseCase.CheckoutCommand;
import com.dw.checkout.domain.exception.FrontendErrorSpec;
import com.dw.checkout.domain.model.CardData;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.CheckoutErrorResponse;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.CheckoutRequest;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.CheckoutSuccessResponse;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.ErrorActionButton;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.FrontendError;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PaymentMethodOption;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PaymentReceipt;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PreCheckoutRequest;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PreCheckoutResponse;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.ProductDetail;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.ProductSlugItem;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PurchasedProductSummary;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class CheckoutWebMapper {

    public List<String> toProductSlugs(PreCheckoutRequest request) {
        return request.getProducts().stream()
                .map(ProductSlugItem::getSlug)
                .toList();
    }

    public PreCheckoutResponse toPreCheckoutResponse(Cart cart) {
        var products = cart.getProducts().stream()
                .map(p -> new ProductDetail()
                        .slug(p.slug())
                        .name(p.name())
                        .description(p.description())
                        .price(p.price().doubleValue())
                        .currency(p.currency()))
                .toList();

        var paymentMethods = cart.getAvailablePaymentMethods().stream()
                .map(pm -> new PaymentMethodOption().name(pm.name()))
                .toList();

        return new PreCheckoutResponse()
                .cartId(cart.getCartId())
                .products(products)
                .paymentMethods(paymentMethods);
    }

    public CheckoutCommand toCommand(CheckoutRequest request) {
        var pmInput = request.getPaymentMethod();
        CardData cardData = null;

        if (pmInput != null && pmInput.getCard() != null) {
            var card = pmInput.getCard();
            cardData = new CardData(
                    card.getHolderName(),
                    card.getNumber(),
                    card.getExpiry(),
                    card.getCvv()
            );
        }

        return new CheckoutCommand(
                request.getCartId(),
                pmInput != null ? pmInput.getName() : null,
                cardData
        );
    }

    public CheckoutSuccessResponse toSuccessResponse(Cart cart) {
        var receipt = cart.getReceipt();

        var paymentReceipt = new PaymentReceipt()
                .method(receipt.method())
                .lastDigits(receipt.lastDigits())
                .pixCode(receipt.pixCode())
                .amount(receipt.amount().doubleValue())
                .currency(receipt.currency());

        var products = cart.getProducts().stream()
                .map(p -> new PurchasedProductSummary()
                        .slug(p.slug())
                        .name(p.name())
                        .price(p.price().doubleValue()))
                .toList();

        return new CheckoutSuccessResponse()
                .cartId(cart.getCartId())
                .status(CheckoutSuccessResponse.StatusEnum.APROVADO)
                .paidAt(OffsetDateTime.ofInstant(receipt.paidAt(), ZoneOffset.UTC))
                .payment(paymentReceipt)
                .products(products);
    }

    public CheckoutErrorResponse toErrorResponse(FrontendErrorSpec spec, UUID cartId) {
        var frontendError = new FrontendError()
                .code(spec.code())
                .title(spec.title())
                .description(spec.description())
                .primaryButton(new ErrorActionButton()
                        .label(spec.primaryButton().label())
                        .url(spec.primaryButton().url()));

        if (spec.secondaryButton() != null) {
            frontendError.setSecondaryButton(new ErrorActionButton()
                    .label(spec.secondaryButton().label())
                    .url(spec.secondaryButton().url()));
        }

        return new CheckoutErrorResponse()
                .displayType(CheckoutErrorResponse.DisplayTypeEnum.fromValue(spec.displayType()))
                .errorCode(spec.errorCode())
                .cartId(cartId)
                .frontendError(frontendError);
    }
}