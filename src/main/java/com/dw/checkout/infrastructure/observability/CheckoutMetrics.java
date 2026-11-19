package com.dw.checkout.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CheckoutMetrics {

    private final MeterRegistry meterRegistry;

    public CheckoutMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Pré-registra as séries principais com 0 para evitar 404 antes do primeiro pagamento
        registerCounter("Cartão de Crédito", "APPROVED");
        registerCounter("Cartão de Crédito", "DECLINED");
        registerCounter("Cartão de Débito", "APPROVED");
        registerCounter("Cartão de Débito", "DECLINED");
        registerCounter("Pix", "APPROVED");
    }

    public void recordPayment(String method, String status) {
        registerCounter(method, status).increment();
    }

    private Counter registerCounter(String method, String status) {
        return Counter.builder("checkout.payments.total")
                .description("Total de pagamentos processados no checkout")
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry);
    }

    public void recordBusinessError(String errorCode) {
        meterRegistry.counter(
                "checkout.errors.total",
                "errorCode", errorCode
        ).increment();
    }
}