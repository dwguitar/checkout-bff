package com.dw.checkout.infrastructure.client;

import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.infrastructure.adapter.out.client.MockProductCatalogAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(MockProductCatalogAdapterCacheTest.CacheTestConfiguration.class)
class MockProductCatalogAdapterCacheTest {

    @Autowired
    private ProductCatalogPort productCatalog;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void doesNotCacheMissingProductWhenCacheRejectsNullValues() {
        assertThat(productCatalog.findBySlug("plano-inexistente")).isEmpty();
        assertThat(productCatalog.findBySlug("plano-inexistente")).isEmpty();
        assertThat(cacheManager.getCache("products").get("plano-inexistente")).isNull();
    }

    @Configuration
    @EnableCaching
    static class CacheTestConfiguration {

        @Bean
        MockProductCatalogAdapter productCatalog() {
            return new MockProductCatalogAdapter();
        }

        @Bean
        CacheManager cacheManager() {
            ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("products");
            cacheManager.setAllowNullValues(false);
            return cacheManager;
        }
    }
}
