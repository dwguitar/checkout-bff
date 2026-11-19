package com.dw.checkout.infrastructure.config;

import com.dw.checkout.application.port.in.CreatePreCheckoutUseCase;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.CheckoutCompletionPort;
import com.dw.checkout.application.port.out.PaymentGatewayPort;
import com.dw.checkout.application.port.out.PaymentMethodPort;
import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.domain.service.CheckoutDomainService;
import com.dw.checkout.domain.service.PreCheckoutDomainService;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;



@Configuration
@EnableCaching
@EnableScheduling
public class BeanConfiguration {

    @Bean
    public CreatePreCheckoutUseCase createPreCheckoutUseCase(ProductCatalogPort productCatalogPort,
                                                             PaymentMethodPort paymentMethodPort,
                                                             CartRepositoryPort cartRepositoryPort) {
        return new PreCheckoutDomainService(productCatalogPort, paymentMethodPort, cartRepositoryPort);
    }

    @Bean
    public CheckoutDomainService checkoutDomainService(CartRepositoryPort cartRepositoryPort,
                                                       PaymentGatewayPort paymentGatewayPort,
                                                       CheckoutCompletionPort checkoutCompletionPort) {
        return new CheckoutDomainService(cartRepositoryPort, paymentGatewayPort, checkoutCompletionPort);
    }

}
