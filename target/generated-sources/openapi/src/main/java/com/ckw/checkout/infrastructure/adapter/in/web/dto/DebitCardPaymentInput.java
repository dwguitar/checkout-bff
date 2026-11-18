package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.CardInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * DebitCardPaymentInput
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class DebitCardPaymentInput implements PaymentMethodInput {

  private String type;

  private CardInput card;

  public DebitCardPaymentInput() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DebitCardPaymentInput(String type, CardInput card) {
    this.type = type;
    this.card = card;
  }

  public DebitCardPaymentInput type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @NotNull @Pattern(regexp = "^DEBIT_CARD$") 
  @Schema(name = "type", example = "DEBIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public DebitCardPaymentInput card(CardInput card) {
    this.card = card;
    return this;
  }

  /**
   * Get card
   * @return card
   */
  @NotNull @Valid 
  @Schema(name = "card", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("card")
  public CardInput getCard() {
    return card;
  }

  public void setCard(CardInput card) {
    this.card = card;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DebitCardPaymentInput debitCardPaymentInput = (DebitCardPaymentInput) o;
    return Objects.equals(this.type, debitCardPaymentInput.type) &&
        Objects.equals(this.card, debitCardPaymentInput.card);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, card);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DebitCardPaymentInput {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    card: ").append(toIndentedString(card)).append("\n");
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

