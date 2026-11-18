package com.dw.checkout.application.port.out;


import com.dw.checkout.domain.event.CheckoutPaymentEvent;

public interface PaymentEventPublisherPort {
    void publish(CheckoutPaymentEvent event);
}