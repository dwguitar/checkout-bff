package com.dw.checkout.application.port.out;


import com.dw.checkout.domain.model.PaymentMethod;

import java.util.List;

public interface PaymentMethodPort {
    List<PaymentMethod> findAvailableMethods();
}
