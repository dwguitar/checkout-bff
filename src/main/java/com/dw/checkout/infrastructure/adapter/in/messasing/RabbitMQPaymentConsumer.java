package com.dw.checkout.infrastructure.adapter.in.messasing;


import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.domain.model.CartStatus;
import com.dw.checkout.infrastructure.config.RabbitMQConfig;
import com.dw.checkout.infrastructure.observability.CheckoutMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPaymentConsumer.class);
    private final CheckoutMetrics checkoutMetrics;

    public RabbitMQPaymentConsumer(CheckoutMetrics checkoutMetrics) {
        this.checkoutMetrics = checkoutMetrics;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void onPaymentProcessed(CheckoutPaymentEvent event) {
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