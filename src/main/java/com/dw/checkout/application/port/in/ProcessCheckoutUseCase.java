package com.dw.checkout.application.port.in;


import com.dw.checkout.domain.model.CardData;
import com.dw.checkout.domain.model.Cart;

import java.util.UUID;

public interface ProcessCheckoutUseCase {
    record CheckoutCommand(UUID cartId, String paymentMethodName, CardData cardData) {}

    Cart execute(CheckoutCommand command);
}