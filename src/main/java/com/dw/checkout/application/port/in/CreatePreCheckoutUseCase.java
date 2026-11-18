package com.dw.checkout.application.port.in;


import com.dw.checkout.domain.model.Cart;

import java.util.List;

public interface CreatePreCheckoutUseCase {
    Cart execute(List<String> productSlugs);
}