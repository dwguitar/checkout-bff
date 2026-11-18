package com.dw.checkout.domain.model;

public record PaymentMethod(String name) {
    public boolean isPix() {
        return "Pix".equalsIgnoreCase(name);
    }

    public boolean isCard() {
        return "Cartão de Crédito".equalsIgnoreCase(name)
                || "Cartão de Débito".equalsIgnoreCase(name);
    }
}
