package com.dw.checkout.domain.exception;


public record FrontendErrorSpec(
        String displayType,
        String errorCode,
        int httpStatus,
        String code,
        String title,
        String description,
        ActionButton primaryButton,
        ActionButton secondaryButton
) {
    public record ActionButton(String label, String url) {}
}