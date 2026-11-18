package com.dw.checkout.infrastructure.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CHECKOUT_EXCHANGE = "checkout.events.exchange";
    public static final String PAYMENT_QUEUE = "checkout.payment.processed.queue";
    public static final String PAYMENT_ROUTING_KEY = "checkout.payment.processed";

    public static final String CHECKOUT_DLX = "checkout.events.dlx";
    public static final String PAYMENT_DLQ = "checkout.payment.processed.dlq";
    public static final String PAYMENT_DLQ_ROUTING_KEY = "checkout.payment.processed.dead";

    @Bean
    public DirectExchange checkoutExchange() {
        return new DirectExchange(CHECKOUT_EXCHANGE);
    }

    @Bean
    public DirectExchange checkoutDeadLetterExchange() {
        return new DirectExchange(CHECKOUT_DLX);
    }

    @Bean
    public Queue paymentProcessedQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", CHECKOUT_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentProcessedDlq() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    public Binding paymentQueueBinding() {
        return BindingBuilder.bind(paymentProcessedQueue())
                .to(checkoutExchange())
                .with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Binding paymentDlqBinding() {
        return BindingBuilder.bind(paymentProcessedDlq())
                .to(checkoutDeadLetterExchange())
                .with(PAYMENT_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
