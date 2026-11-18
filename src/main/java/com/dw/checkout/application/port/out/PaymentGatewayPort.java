package com.dw.checkout.application.port.out;

import com.dw.checkout.domain.model.CardData;
import com.dw.checkout.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayPort {
    record PaymentResult(boolean approved, String pixCode) {}

    PaymentResult charge(UUID cartId, PaymentMethod method, CardData cardData, BigDecimal amount);
}