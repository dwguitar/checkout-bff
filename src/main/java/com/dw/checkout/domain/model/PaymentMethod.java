package com.dw.checkout.domain.model;

import java.io.Serializable;

public record PaymentMethod(String name) implements Serializable {
    public boolean isPix() {
        return "Pix".equalsIgnoreCase(name);
    }

    public boolean isCard() {
        return "Cartão de Crédito".equalsIgnoreCase(name)
                || "Cartão de Débito".equalsIgnoreCase(name);
    }
}
