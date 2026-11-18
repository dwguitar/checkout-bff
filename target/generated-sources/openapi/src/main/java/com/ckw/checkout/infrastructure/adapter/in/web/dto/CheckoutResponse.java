package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.PaymentSummary;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.ProductSummary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CheckoutResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class CheckoutResponse {

  private UUID cartId;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    APROVADO("APROVADO");

    private final String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime paidAt;

  private PaymentSummary payment;

  @Valid
  private List<@Valid ProductSummary> products = new ArrayList<>();

  public CheckoutResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CheckoutResponse(UUID cartId, StatusEnum status, OffsetDateTime paidAt, PaymentSummary payment, List<@Valid ProductSummary> products) {
    this.cartId = cartId;
    this.status = status;
    this.paidAt = paidAt;
    this.payment = payment;
    this.products = products;
  }

  public CheckoutResponse cartId(UUID cartId) {
    this.cartId = cartId;
    return this;
  }

  /**
   * Get cartId
   * @return cartId
   */
  @NotNull @Valid 
  @Schema(name = "cartId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("cartId")
  public UUID getCartId() {
    return cartId;
  }

  public void setCartId(UUID cartId) {
    this.cartId = cartId;
  }

  public CheckoutResponse status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public CheckoutResponse paidAt(OffsetDateTime paidAt) {
    this.paidAt = paidAt;
    return this;
  }

  /**
   * Get paidAt
   * @return paidAt
   */
  @NotNull @Valid 
  @Schema(name = "paidAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("paidAt")
  public OffsetDateTime getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(OffsetDateTime paidAt) {
    this.paidAt = paidAt;
  }

  public CheckoutResponse payment(PaymentSummary payment) {
    this.payment = payment;
    return this;
  }

  /**
   * Get payment
   * @return payment
   */
  @NotNull @Valid 
  @Schema(name = "payment", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("payment")
  public PaymentSummary getPayment() {
    return payment;
  }

  public void setPayment(PaymentSummary payment) {
    this.payment = payment;
  }

  public CheckoutResponse products(List<@Valid ProductSummary> products) {
    this.products = products;
    return this;
  }

  public CheckoutResponse addProductsItem(ProductSummary productsItem) {
    if (this.products == null) {
      this.products = new ArrayList<>();
    }
    this.products.add(productsItem);
    return this;
  }

  /**
   * Get products
   * @return products
   */
  @NotNull @Valid 
  @Schema(name = "products", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("products")
  public List<@Valid ProductSummary> getProducts() {
    return products;
  }

  public void setProducts(List<@Valid ProductSummary> products) {
    this.products = products;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheckoutResponse checkoutResponse = (CheckoutResponse) o;
    return Objects.equals(this.cartId, checkoutResponse.cartId) &&
        Objects.equals(this.status, checkoutResponse.status) &&
        Objects.equals(this.paidAt, checkoutResponse.paidAt) &&
        Objects.equals(this.payment, checkoutResponse.payment) &&
        Objects.equals(this.products, checkoutResponse.products);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cartId, status, paidAt, payment, products);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CheckoutResponse {\n");
    sb.append("    cartId: ").append(toIndentedString(cartId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    paidAt: ").append(toIndentedString(paidAt)).append("\n");
    sb.append("    payment: ").append(toIndentedString(payment)).append("\n");
    sb.append("    products: ").append(toIndentedString(products)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

