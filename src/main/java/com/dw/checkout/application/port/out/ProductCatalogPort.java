package com.dw.checkout.application.port.out;

import com.dw.checkout.domain.model.Product;

import java.util.Optional;

public interface ProductCatalogPort {
    Optional<Product> findBySlug(String slug);
}