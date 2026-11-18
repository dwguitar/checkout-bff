package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CardInput
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class CardInput {

  private String holderName;

  private String number;

  private String expiration;

  private String cvv;

  public CardInput() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CardInput(String holderName, String number, String expiration, String cvv) {
    this.holderName = holderName;
    this.number = number;
    this.expiration = expiration;
    this.cvv = cvv;
  }

  public CardInput holderName(String holderName) {
    this.holderName = holderName;
    return this;
  }

  /**
   * Get holderName
   * @return holderName
   */
  @NotNull @Size(min = 3, max = 80) 
  @Schema(name = "holderName", example = "Maria Silva", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("holderName")
  public String getHolderName() {
    return holderName;
  }

  public void setHolderName(String holderName) {
    this.holderName = holderName;
  }

  public CardInput number(String number) {
    this.number = number;
    return this;
  }

  /**
   * PAN. Usado apenas em memória para autorizar; nunca é persistido nem logado.
   * @return number
   */
  @NotNull @Pattern(regexp = "^[0-9]{13,19}$") 
  @Schema(name = "number", example = "4111111111111111", description = "PAN. Usado apenas em memória para autorizar; nunca é persistido nem logado.", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("number")
  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public CardInput expiration(String expiration) {
    this.expiration = expiration;
    return this;
  }

  /**
   * Validade no formato MM/AA
   * @return expiration
   */
  @NotNull @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$") 
  @Schema(name = "expiration", example = "12/30", description = "Validade no formato MM/AA", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("expiration")
  public String getExpiration() {
    return expiration;
  }

  public void setExpiration(String expiration) {
    this.expiration = expiration;
  }

  public CardInput cvv(String cvv) {
    this.cvv = cvv;
    return this;
  }

  /**
   * Get cvv
   * @return cvv
   */
  @NotNull @Pattern(regexp = "^[0-9]{3,4}$") 
  @Schema(name = "cvv", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("cvv")
  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardInput cardInput = (CardInput) o;
    return Objects.equals(this.holderName, cardInput.holderName) &&
        Objects.equals(this.number, cardInput.number) &&
        Objects.equals(this.expiration, cardInput.expiration) &&
        Objects.equals(this.cvv, cardInput.cvv);
  }

  @Override
  public int hashCode() {
    return Objects.hash(holderName, number, expiration, cvv);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardInput {\n");
    sb.append("    holderName: ").append(toIndentedString(holderName)).append("\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    expiration: ").append(toIndentedString(expiration)).append("\n");
    sb.append("    cvv: ").append(toIndentedString(cvv)).append("\n");
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

