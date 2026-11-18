package com.dw.checkout.infrastructure.adapter.out.client;


import com.dw.checkout.application.port.out.PaymentMethodPort;
import com.dw.checkout.domain.model.PaymentMethod;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockPaymentMethodAdapter implements PaymentMethodPort {

    @Override
    @Cacheable("paymentMethods")
    public List<PaymentMethod> findAvailableMethods() {
        return List.of(
                new PaymentMethod("Cartão de Crédito"),
                new PaymentMethod("Cartão de Débito"),
                new PaymentMethod("Pix")
        );
    }
}
