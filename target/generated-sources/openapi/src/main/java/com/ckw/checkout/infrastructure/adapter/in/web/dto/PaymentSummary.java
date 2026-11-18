package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PaymentSummary
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class PaymentSummary {

  private @Nullable String lastDigits;

  private @Nullable String pixCode;

  private BigDecimal amount;

  private String currency;

  public PaymentSummary() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PaymentSummary(BigDecimal amount, String currency) {
    this.amount = amount;
    this.currency = currency;
  }

  public PaymentSummary lastDigits(@Nullable String lastDigits) {
    this.lastDigits = lastDigits;
    return this;
  }

  /**
   * Quatro últimos dígitos, presente quando o pagamento é cartão
   * @return lastDigits
   */
  @Size(min = 4, max = 4) 
  @Schema(name = "lastDigits", example = "1111", description = "Quatro últimos dígitos, presente quando o pagamento é cartão", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastDigits")
  public @Nullable String getLastDigits() {
    return lastDigits;
  }

  public void setLastDigits(@Nullable String lastDigits) {
    this.lastDigits = lastDigits;
  }

  public PaymentSummary pixCode(@Nullable String pixCode) {
    this.pixCode = pixCode;
    return this;
  }

  /**
   * Código Pix simulado, presente quando o pagamento é Pix
   * @return pixCode
   */
  
  @Schema(name = "pixCode", description = "Código Pix simulado, presente quando o pagamento é Pix", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("pixCode")
  public @Nullable String getPixCode() {
    return pixCode;
  }

  public void setPixCode(@Nullable String pixCode) {
    this.pixCode = pixCode;
  }

  public PaymentSummary amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
   */
  @NotNull @Valid 
  @Schema(name = "amount", example = "39.9", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amount")
  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public PaymentSummary currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
   */
  @NotNull 
  @Schema(name = "currency", example = "BRL", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentSummary paymentSummary = (PaymentSummary) o;
    return Objects.equals(this.lastDigits, paymentSummary.lastDigits) &&
        Objects.equals(this.pixCode, paymentSummary.pixCode) &&
        Objects.equals(this.amount, paymentSummary.amount) &&
        Objects.equals(this.currency, paymentSummary.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastDigits, pixCode, amount, currency);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentSummary {\n");
    sb.append("    lastDigits: ").append(toIndentedString(lastDigits)).append("\n");
    sb.append("    pixCode: ").append(toIndentedString(pixCode)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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

