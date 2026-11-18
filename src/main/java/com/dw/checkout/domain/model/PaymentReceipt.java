package com.dw.checkout.domain.model;


import java.math.BigDecimal;
import java.time.Instant;

public record PaymentReceipt(
        String method,
        String lastDigits,
        String pixCode,
        BigDecimal amount,
        String currency,
        Instant paidAt
) {}
