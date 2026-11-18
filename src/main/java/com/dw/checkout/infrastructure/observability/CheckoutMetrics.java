package com.dw.checkout.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CheckoutMetrics {

    private final MeterRegistry meterRegistry;

    public CheckoutMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPaymentAttempt(String paymentMethod, String status) {
        meterRegistry.counter(
                "checkout.payments.total",
                "method", paymentMethod,
                "status", status
        ).increment();
    }

    public void recordBusinessError(String errorCode) {
        meterRegistry.counter(
                "checkout.errors.total",
                "errorCode", errorCode
        ).increment();
    }
}