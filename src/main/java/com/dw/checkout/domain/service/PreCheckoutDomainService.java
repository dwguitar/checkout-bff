package com.dw.checkout.domain.service;


import com.dw.checkout.application.port.in.CreatePreCheckoutUseCase;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.PaymentMethodPort;
import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.domain.exception.CheckoutBusinessException;
import com.dw.checkout.domain.exception.ErrorCatalog;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.Product;

import java.util.List;

public class PreCheckoutDomainService implements CreatePreCheckoutUseCase {

    private final ProductCatalogPort productCatalogPort;
    private final PaymentMethodPort paymentMethodPort;
    private final CartRepositoryPort cartRepositoryPort;

    public PreCheckoutDomainService(ProductCatalogPort productCatalogPort,
                                    PaymentMethodPort paymentMethodPort,
                                    CartRepositoryPort cartRepositoryPort) {
        this.productCatalogPort = productCatalogPort;
        this.paymentMethodPort = paymentMethodPort;
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Cart execute(List<String> productSlugs) {
        if (productSlugs == null || productSlugs.isEmpty()) {
            throw new CheckoutBusinessException(ErrorCatalog.PRODUCT_NOT_FOUND, null);
        }
        List<Product> products = productSlugs.stream()
                .map(slug -> productCatalogPort.findBySlug(slug)
                        .orElseThrow(() -> new CheckoutBusinessException(ErrorCatalog.PRODUCT_NOT_FOUND, null)))
                .toList();

        List<PaymentMethod> paymentMethods = paymentMethodPort.findAvailableMethods();
        Cart cart = Cart.createNew(products, paymentMethods);

        return cartRepositoryPort.save(cart);
    }
}
