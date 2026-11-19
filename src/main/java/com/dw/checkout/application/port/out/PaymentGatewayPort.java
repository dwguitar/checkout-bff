package com.dw.checkout.application.port.out;

import com.dw.checkout.domain.model.CardData;
import com.dw.checkout.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;

public interface PaymentGatewayPort {
    record PaymentResult(boolean approved, String pixCode) {}
    record PaymentAttempt(UUID cartId, UUID attemptId, String method, String cardLastDigits,
                          BigDecimal amount, String currency) {}

    /** Implementations must use attemptId as the gateway idempotency key. */
    PaymentResult charge(UUID cartId, UUID attemptId, PaymentMethod method, CardData cardData, BigDecimal amount);

    Optional<PaymentResult> findPaymentStatus(PaymentAttempt attempt);
}
