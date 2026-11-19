package com.dw.checkout.infrastructure.adapter.in.messasing;


import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.domain.model.CartStatus;
import com.dw.checkout.infrastructure.config.RabbitMQConfig;
import com.dw.checkout.infrastructure.observability.CheckoutMetrics;
import com.dw.checkout.infrastructure.adapter.out.persistence.ProcessedPaymentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class RabbitMQPaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPaymentConsumer.class);
    private final CheckoutMetrics checkoutMetrics;
    private final ProcessedPaymentEventRepository processedEventRepository;

    public RabbitMQPaymentConsumer(CheckoutMetrics checkoutMetrics,
                                  ProcessedPaymentEventRepository processedEventRepository) {
        this.checkoutMetrics = checkoutMetrics;
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    @Transactional
    public void onPaymentProcessed(CheckoutPaymentEvent event) {
        if (processedEventRepository.markProcessedIfNew(event.eventId(), Instant.now()) == 0) {
            log.info("Evento repetido ignorado | eventId={} | cartId={}", event.eventId(), event.cartId());
            return;
        }
        if (event.status() == CartStatus.APPROVED) {
            checkoutMetrics.recordPayment(event.paymentMethod(), event.status().name());
            log.info("[RabbitMQ Consumer] Ativando assinatura assíncrona | cartId={} | planos={} | valor={} {}",
                    event.cartId(), event.productSlugs(), event.amount(), event.currency());
        } else {
            log.warn("[RabbitMQ Consumer] Registrando tentativa recusada para análise antifraude/retentativa | cartId={} | metodo={}",
                    event.cartId(), event.paymentMethod());
        }
    }
}
