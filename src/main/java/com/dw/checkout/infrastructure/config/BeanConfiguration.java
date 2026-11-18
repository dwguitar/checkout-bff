package com.dw.checkout.infrastructure.config;

import com.dw.checkout.application.port.in.CreatePreCheckoutUseCase;
import com.dw.checkout.application.port.in.ProcessCheckoutUseCase;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.PaymentEventPublisherPort;
import com.dw.checkout.application.port.out.PaymentGatewayPort;
import com.dw.checkout.application.port.out.PaymentMethodPort;
import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.domain.service.CheckoutDomainService;
import com.dw.checkout.domain.service.PreCheckoutDomainService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableCaching
public class BeanConfiguration {

    @Bean
    public CreatePreCheckoutUseCase createPreCheckoutUseCase(ProductCatalogPort productCatalogPort,
                                                             PaymentMethodPort paymentMethodPort,
                                                             CartRepositoryPort cartRepositoryPort) {
        return new PreCheckoutDomainService(productCatalogPort, paymentMethodPort, cartRepositoryPort);
    }

    @Bean
    public ProcessCheckoutUseCase processCheckoutUseCase(CartRepositoryPort cartRepositoryPort,
                                                         PaymentGatewayPort paymentGatewayPort,
                                                         PaymentEventPublisherPort paymentEventPublisherPort) {
        return new CheckoutDomainService(cartRepositoryPort, paymentGatewayPort, paymentEventPublisherPort);
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager =
                new CaffeineCacheManager("products", "paymentMethods");

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES));

        return cacheManager;
    }
}