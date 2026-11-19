package com.dw.checkout.infrastructure.adapter.out.client;


import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.domain.model.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class MockProductCatalogAdapter implements ProductCatalogPort {

    private static final Map<String, Product> CATALOG = Map.of(
            "basico", new Product(
                    "basico",
                    "Plano Básico",
                    "Assinatura mensal com acesso padrão",
                    new BigDecimal("19.90"),
                    "BRL"
            ),
            "premium", new Product(
                    "premium",
                    "Plano Premium",
                    "Assinatura mensal com catálogo completo",
                    new BigDecimal("39.90"),
                    "BRL"
            ),
            "familia", new Product(
                    "familia",
                    "Plano Família",
                    "Assinatura mensal para até 5 perfis simultâneos",
                    new BigDecimal("59.90"),
                    "BRL"
            )
    );

    @Override
    // Optional.empty() is unwrapped to null by Spring's cache abstraction.
    // Do not cache that miss: RedisCache is configured to reject null values.
    @Cacheable(value = "products", key = "#slug.toLowerCase()", unless = "#result == null")
    public Optional<Product> findBySlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CATALOG.get(slug.toLowerCase()));
    }
}
