package com.dw.checkout.domain.model;

public record CardData(
        String holderName,
        String number,
        String expiry,
        String cvv
) {
    public String lastDigits() {
        if (number == null || number.length() < 4) {
            return "";
        }
        return number.substring(number.length() - 4);
    }

    public boolean endsWith(String suffix) {
        return number != null && number.endsWith(suffix);
    }
}