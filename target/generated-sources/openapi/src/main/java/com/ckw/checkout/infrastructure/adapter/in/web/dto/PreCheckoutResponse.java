package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.PaymentMethodOption;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.ProductDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PreCheckoutResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class PreCheckoutResponse {

  private UUID cartId;

  @Valid
  private List<@Valid ProductDetails> products = new ArrayList<>();

  @Valid
  private List<@Valid PaymentMethodOption> paymentMethods = new ArrayList<>();

  public PreCheckoutResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PreCheckoutResponse(UUID cartId, List<@Valid ProductDetails> products, List<@Valid PaymentMethodOption> paymentMethods) {
    this.cartId = cartId;
    this.products = products;
    this.paymentMethods = paymentMethods;
  }

  public PreCheckoutResponse cartId(UUID cartId) {
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

  public PreCheckoutResponse products(List<@Valid ProductDetails> products) {
    this.products = products;
    return this;
  }

  public PreCheckoutResponse addProductsItem(ProductDetails productsItem) {
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
  public List<@Valid ProductDetails> getProducts() {
    return products;
  }

  public void setProducts(List<@Valid ProductDetails> products) {
    this.products = products;
  }

  public PreCheckoutResponse paymentMethods(List<@Valid PaymentMethodOption> paymentMethods) {
    this.paymentMethods = paymentMethods;
    return this;
  }

  public PreCheckoutResponse addPaymentMethodsItem(PaymentMethodOption paymentMethodsItem) {
    if (this.paymentMethods == null) {
      this.paymentMethods = new ArrayList<>();
    }
    this.paymentMethods.add(paymentMethodsItem);
    return this;
  }

  /**
   * Get paymentMethods
   * @return paymentMethods
   */
  @NotNull @Valid 
  @Schema(name = "paymentMethods", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("paymentMethods")
  public List<@Valid PaymentMethodOption> getPaymentMethods() {
    return paymentMethods;
  }

  public void setPaymentMethods(List<@Valid PaymentMethodOption> paymentMethods) {
    this.paymentMethods = paymentMethods;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PreCheckoutResponse preCheckoutResponse = (PreCheckoutResponse) o;
    return Objects.equals(this.cartId, preCheckoutResponse.cartId) &&
        Objects.equals(this.products, preCheckoutResponse.products) &&
        Objects.equals(this.paymentMethods, preCheckoutResponse.paymentMethods);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cartId, products, paymentMethods);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PreCheckoutResponse {\n");
    sb.append("    cartId: ").append(toIndentedString(cartId)).append("\n");
    sb.append("    products: ").append(toIndentedString(products)).append("\n");
    sb.append("    paymentMethods: ").append(toIndentedString(paymentMethods)).append("\n");
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

