package com.dw.checkout.domain.exception;


import java.util.UUID;

public class CheckoutBusinessException extends RuntimeException {

    private final FrontendErrorSpec errorSpec;
    private final UUID cartId;

    public CheckoutBusinessException(FrontendErrorSpec errorSpec, UUID cartId) {
        super(errorSpec.errorCode() + ": " + errorSpec.description());
        this.errorSpec = errorSpec;
        this.cartId = cartId;
    }

    public FrontendErrorSpec getErrorSpec() { return errorSpec; }
    public UUID getCartId() { return cartId; }
}