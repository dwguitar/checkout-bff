package com.dw.checkout.domain.model;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Cart {

    private final UUID cartId;
    private final List<Product> products;
    private final List<PaymentMethod> availablePaymentMethods;
    private CartStatus status;
    private PaymentReceipt receipt;

    public Cart(UUID cartId,
                List<Product> products,
                List<PaymentMethod> availablePaymentMethods,
                CartStatus status,
                PaymentReceipt receipt) {
        this.cartId = cartId;
        this.products = List.copyOf(products);
        this.availablePaymentMethods = List.copyOf(availablePaymentMethods);
        this.status = status;
        this.receipt = receipt;
    }

    public static Cart createNew(List<Product> products, List<PaymentMethod> paymentMethods) {
        return new Cart(UUID.randomUUID(), products, paymentMethods, CartStatus.CREATED, null);
    }

    public boolean isApproved() {
        return this.status == CartStatus.APPROVED;
    }

    public boolean supportsPaymentMethod(String methodName) {
        return availablePaymentMethods.stream()
                .anyMatch(pm -> pm.name().equalsIgnoreCase(methodName));
    }

    public BigDecimal totalAmount() {
        return products.stream()
                .map(Product::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String currency() {
        return products.isEmpty() ? "BRL" : products.getFirst().currency();
    }

    public void approve(PaymentReceipt receipt) {
        this.status = CartStatus.APPROVED;
        this.receipt = receipt;
    }

    public void decline() {
        this.status = CartStatus.DECLINED;
    }

    public UUID getCartId() { return cartId; }
    public List<Product> getProducts() { return products; }
    public List<PaymentMethod> getAvailablePaymentMethods() { return availablePaymentMethods; }
    public CartStatus getStatus() { return status; }
    public PaymentReceipt getReceipt() { return receipt; }
}
