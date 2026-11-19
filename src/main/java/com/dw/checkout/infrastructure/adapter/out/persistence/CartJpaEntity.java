package com.dw.checkout.infrastructure.adapter.out.persistence;

import com.dw.checkout.domain.model.CartStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class CartJpaEntity {

    @Id
    @Column(name = "cart_id", nullable = false, updatable = false)
    private UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CartStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_products", joinColumns = @JoinColumn(name = "cart_id"))
    private List<ProductEmbeddable> products = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_payment_methods", joinColumns = @JoinColumn(name = "cart_id"))
    @Column(name = "method_name")
    private List<String> availablePaymentMethods = new ArrayList<>();

    // Dados seguros do recibo (nunca armazena número completo do cartão ou CVV)
    @Column(name = "paid_method")
    private String paidMethod;

    @Column(name = "card_last_digits", length = 4)
    private String cardLastDigits;

    @Column(name = "pix_code", length = 512)
    private String pixCode;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payment_attempt_method")
    private String paymentAttemptMethod;

    @Column(name = "payment_attempt_card_last_digits", length = 4)
    private String paymentAttemptCardLastDigits;

    @Column(name = "payment_processing_at")
    private Instant paymentProcessingAt;

    @Column(name = "payment_attempt_id")
    private UUID paymentAttemptId;

    public CartJpaEntity() {}

    @Embeddable
    public static class ProductEmbeddable {
        private String slug;
        private String name;
        private String description;
        private BigDecimal price;
        private String currency;

        public ProductEmbeddable() {}

        public ProductEmbeddable(String slug, String name, String description, BigDecimal price, String currency) {
            this.slug = slug;
            this.name = name;
            this.description = description;
            this.price = price;
            this.currency = currency;
        }

        public String getSlug() { return slug; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public BigDecimal getPrice() { return price; }
        public String getCurrency() { return currency; }
    }

    public UUID getCartId() { return cartId; }
    public void setCartId(UUID cartId) { this.cartId = cartId; }
    public CartStatus getStatus() { return status; }
    public void setStatus(CartStatus status) { this.status = status; }
    public List<ProductEmbeddable> getProducts() { return products; }
    public void setProducts(List<ProductEmbeddable> products) { this.products = products; }
    public List<String> getAvailablePaymentMethods() { return availablePaymentMethods; }
    public void setAvailablePaymentMethods(List<String> availablePaymentMethods) { this.availablePaymentMethods = availablePaymentMethods; }
    public String getPaidMethod() { return paidMethod; }
    public void setPaidMethod(String paidMethod) { this.paidMethod = paidMethod; }
    public String getCardLastDigits() { return cardLastDigits; }
    public void setCardLastDigits(String cardLastDigits) { this.cardLastDigits = cardLastDigits; }
    public String getPixCode() { return pixCode; }
    public void setPixCode(String pixCode) { this.pixCode = pixCode; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getPaymentAttemptMethod() { return paymentAttemptMethod; }
    public void setPaymentAttemptMethod(String paymentAttemptMethod) { this.paymentAttemptMethod = paymentAttemptMethod; }
    public String getPaymentAttemptCardLastDigits() { return paymentAttemptCardLastDigits; }
    public void setPaymentAttemptCardLastDigits(String paymentAttemptCardLastDigits) { this.paymentAttemptCardLastDigits = paymentAttemptCardLastDigits; }
    public Instant getPaymentProcessingAt() { return paymentProcessingAt; }
    public void setPaymentProcessingAt(Instant paymentProcessingAt) { this.paymentProcessingAt = paymentProcessingAt; }
    public UUID getPaymentAttemptId() { return paymentAttemptId; }
    public void setPaymentAttemptId(UUID paymentAttemptId) { this.paymentAttemptId = paymentAttemptId; }
}
