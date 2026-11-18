package com.dw.checkout.infrastructure.adapter.out.persistence;


import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.PaymentReceipt;
import com.dw.checkout.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CartPersistenceAdapter implements CartRepositoryPort {

    private final SpringDataCartRepository repository;

    public CartPersistenceAdapter(SpringDataCartRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity entity = toEntity(cart);
        CartJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Cart> findById(UUID cartId) {
        return repository.findById(cartId).map(this::toDomain);
    }

    private CartJpaEntity toEntity(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.setCartId(cart.getCartId());
        entity.setStatus(cart.getStatus());
        entity.setProducts(cart.getProducts().stream()
                .map(p -> new CartJpaEntity.ProductEmbeddable(p.slug(), p.name(), p.description(), p.price(), p.currency()))
                .toList());
        entity.setAvailablePaymentMethods(cart.getAvailablePaymentMethods().stream()
                .map(PaymentMethod::name)
                .toList());

        if (cart.getReceipt() != null) {
            PaymentReceipt r = cart.getReceipt();
            entity.setPaidMethod(r.method());
            entity.setCardLastDigits(r.lastDigits());
            entity.setPixCode(r.pixCode());
            entity.setPaidAmount(r.amount());
            entity.setCurrency(r.currency());
            entity.setPaidAt(r.paidAt());
        }
        return entity;
    }

    private Cart toDomain(CartJpaEntity entity) {
        var products = entity.getProducts().stream()
                .map(p -> new Product(p.getSlug(), p.getName(), p.getDescription(), p.getPrice(), p.getCurrency()))
                .toList();

        var methods = entity.getAvailablePaymentMethods().stream()
                .map(PaymentMethod::new)
                .toList();

        PaymentReceipt receipt = null;
        if (entity.getPaidMethod() != null) {
            receipt = new PaymentReceipt(
                    entity.getPaidMethod(),
                    entity.getCardLastDigits(),
                    entity.getPixCode(),
                    entity.getPaidAmount(),
                    entity.getCurrency(),
                    entity.getPaidAt()
            );
        }

        return new Cart(entity.getCartId(), products, methods, entity.getStatus(), receipt);
    }
}