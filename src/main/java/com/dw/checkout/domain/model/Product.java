package com.dw.checkout.domain.model;

import java.math.BigDecimal;

public record Product(
        String slug,
        String name,
        String description,
        BigDecimal price,
        String currency
) {}