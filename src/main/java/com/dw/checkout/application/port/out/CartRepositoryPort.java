package com.dw.checkout.application.port.out;

import com.dw.checkout.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepositoryPort {
    Cart save(Cart cart);
    Optional<Cart> findById(UUID cartId);
    /** Atomically reserves a cart for payment when it is eligible for a new attempt. */
    boolean tryStartPayment(UUID cartId);
}
