package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.DisplayType;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.FrontendError;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CheckoutErrorResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class CheckoutErrorResponse {

  private DisplayType displayType;

  private String errorCode;

  private @Nullable UUID cartId;

  private FrontendError frontendError;

  public CheckoutErrorResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CheckoutErrorResponse(DisplayType displayType, String errorCode, FrontendError frontendError) {
    this.displayType = displayType;
    this.errorCode = errorCode;
    this.frontendError = frontendError;
  }

  public CheckoutErrorResponse displayType(DisplayType displayType) {
    this.displayType = displayType;
    return this;
  }

  /**
   * Get displayType
   * @return displayType
   */
  @NotNull @Valid 
  @Schema(name = "displayType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("displayType")
  public DisplayType getDisplayType() {
    return displayType;
  }

  public void setDisplayType(DisplayType displayType) {
    this.displayType = displayType;
  }

  public CheckoutErrorResponse errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  /**
   * Get errorCode
   * @return errorCode
   */
  @NotNull 
  @Schema(name = "errorCode", example = "P-1001", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("errorCode")
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public CheckoutErrorResponse cartId(@Nullable UUID cartId) {
    this.cartId = cartId;
    return this;
  }

  /**
   * Get cartId
   * @return cartId
   */
  @Valid 
  @Schema(name = "cartId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cartId")
  public @Nullable UUID getCartId() {
    return cartId;
  }

  public void setCartId(@Nullable UUID cartId) {
    this.cartId = cartId;
  }

  public CheckoutErrorResponse frontendError(FrontendError frontendError) {
    this.frontendError = frontendError;
    return this;
  }

  /**
   * Get frontendError
   * @return frontendError
   */
  @NotNull @Valid 
  @Schema(name = "frontendError", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("frontendError")
  public FrontendError getFrontendError() {
    return frontendError;
  }

  public void setFrontendError(FrontendError frontendError) {
    this.frontendError = frontendError;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheckoutErrorResponse checkoutErrorResponse = (CheckoutErrorResponse) o;
    return Objects.equals(this.displayType, checkoutErrorResponse.displayType) &&
        Objects.equals(this.errorCode, checkoutErrorResponse.errorCode) &&
        Objects.equals(this.cartId, checkoutErrorResponse.cartId) &&
        Objects.equals(this.frontendError, checkoutErrorResponse.frontendError);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayType, errorCode, cartId, frontendError);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CheckoutErrorResponse {\n");
    sb.append("    displayType: ").append(toIndentedString(displayType)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
    sb.append("    cartId: ").append(toIndentedString(cartId)).append("\n");
    sb.append("    frontendError: ").append(toIndentedString(frontendError)).append("\n");
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

