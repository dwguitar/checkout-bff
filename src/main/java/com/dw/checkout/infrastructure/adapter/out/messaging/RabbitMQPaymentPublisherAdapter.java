package com.dw.checkout.infrastructure.adapter.out.messaging;


import com.dw.checkout.application.port.out.PaymentEventPublisherPort;
import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPaymentPublisherAdapter implements PaymentEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPaymentPublisherAdapter.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPaymentPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(CheckoutPaymentEvent event) {
        log.info("[RabbitMQ Producer] Publicando evento de checkout | eventId={} | cartId={} | status={}",
                event.eventId(), event.cartId(), event.status());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHECKOUT_EXCHANGE,
                RabbitMQConfig.PAYMENT_ROUTING_KEY,
                event
        );
    }
}