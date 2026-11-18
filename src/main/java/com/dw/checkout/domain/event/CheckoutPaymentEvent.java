package com.dw.checkout.domain.event;


import com.dw.checkout.domain.model.CartStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CheckoutPaymentEvent(
        UUID eventId,
        UUID cartId,
        CartStatus status,
        String paymentMethod,
        BigDecimal amount,
        String currency,
        List<String> productSlugs,
        Instant occurredAt
) {
    public static CheckoutPaymentEvent of(UUID cartId,
                                          CartStatus status,
                                          String paymentMethod,
                                          BigDecimal amount,
                                          String currency,
                                          List<String> productSlugs) {
        return new CheckoutPaymentEvent(
                UUID.randomUUID(),
                cartId,
                status,
                paymentMethod,
                amount,
                currency,
                productSlugs,
                Instant.now()
        );
    }
}