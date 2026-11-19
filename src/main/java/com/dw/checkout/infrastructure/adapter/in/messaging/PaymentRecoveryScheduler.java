package com.dw.checkout.infrastructure.adapter.in.messaging;

import com.dw.checkout.application.port.in.RecoverStuckPaymentsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaymentRecoveryScheduler {
    private static final Logger log = LoggerFactory.getLogger(PaymentRecoveryScheduler.class);
    private final RecoverStuckPaymentsUseCase recoveryUseCase;
    private final long timeoutMs;

    public PaymentRecoveryScheduler(RecoverStuckPaymentsUseCase recoveryUseCase,
                                    @Value("${checkout.payment-recovery.timeout-ms:120000}") long timeoutMs) {
        this.recoveryUseCase = recoveryUseCase;
        this.timeoutMs = timeoutMs;
    }

    @Scheduled(fixedDelayString = "${checkout.payment-recovery.interval-ms:30000}")
    public void recoverStuckPayments() {
        try {
            recoveryUseCase.recoverBefore(Instant.now().minusMillis(timeoutMs));
        } catch (Exception ex) {
            log.error("Falha durante reconciliação de pagamentos em processamento", ex);
        }
    }
}
