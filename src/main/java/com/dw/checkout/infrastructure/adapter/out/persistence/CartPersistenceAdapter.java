package com.dw.checkout.infrastructure.adapter.out.persistence;


import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.CheckoutCompletionPort;
import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.PaymentReceipt;
import com.dw.checkout.domain.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Component
public class CartPersistenceAdapter implements CartRepositoryPort, CheckoutCompletionPort {

    private final SpringDataCartRepository repository;
    private final CheckoutOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CartPersistenceAdapter(SpringDataCartRepository repository,
                                  CheckoutOutboxRepository outboxRepository,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
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

    @Override
    @Transactional
    public boolean tryStartPayment(UUID cartId, String paymentMethod, String cardLastDigits, UUID attemptId) {
        return repository.claimForPayment(cartId, paymentMethod, cardLastDigits, attemptId) == 1;
    }

    @Override
    @Transactional
    public boolean tryClaimStuckPaymentRecovery(UUID cartId, java.time.Instant cutoff) {
        return repository.claimStuckRecovery(cartId, cutoff) == 1;
    }

    @Override
    public java.util.List<Cart> findStuckProcessingBefore(java.time.Instant cutoff) {
        return repository.findTop100ByStatusAndPaymentProcessingAtBeforeOrderByPaymentProcessingAtAsc(
                        com.dw.checkout.domain.model.CartStatus.PROCESSING, cutoff)
                .stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public Cart saveAndEnqueue(Cart cart, CheckoutPaymentEvent event) {
        try {
            int inserted = outboxRepository.enqueuePending(event.eventId(), cart.getCartId(),
                    cart.getPaymentAttemptId(), objectMapper.writeValueAsString(event), java.time.Instant.now());
            if (inserted == 0) {
                return repository.findById(cart.getCartId()).map(this::toDomain).orElseThrow();
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Não foi possível serializar evento de checkout", ex);
        }
        return save(cart);
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
        entity.setPaymentAttemptMethod(cart.getPaymentAttemptMethod());
        entity.setPaymentAttemptCardLastDigits(cart.getPaymentAttemptCardLastDigits());
        entity.setPaymentProcessingAt(cart.getPaymentProcessingAt());
        entity.setPaymentAttemptId(cart.getPaymentAttemptId());
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

        return new Cart(entity.getCartId(), products, methods, entity.getStatus(), receipt,
                entity.getPaymentAttemptMethod(), entity.getPaymentAttemptCardLastDigits(), entity.getPaymentProcessingAt(),
                entity.getPaymentAttemptId());
    }
}
