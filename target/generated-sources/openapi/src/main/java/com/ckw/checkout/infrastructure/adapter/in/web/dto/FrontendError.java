package com.ckw.checkout.infrastructure.adapter.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.ckw.checkout.infrastructure.adapter.in.web.dto.FrontendButton;
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
 * FrontendError
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.14.0")
public class FrontendError {

  private String code;

  private String title;

  private String description;

  private FrontendButton primaryButton;

  private @Nullable FrontendButton secondaryButton;

  public FrontendError() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public FrontendError(String code, String title, String description, FrontendButton primaryButton) {
    this.code = code;
    this.title = title;
    this.description = description;
    this.primaryButton = primaryButton;
  }

  public FrontendError code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @NotNull 
  @Schema(name = "code", example = "P-1001", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public FrontendError title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
   */
  @NotNull 
  @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public FrontendError description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  @NotNull 
  @Schema(name = "description", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public FrontendError primaryButton(FrontendButton primaryButton) {
    this.primaryButton = primaryButton;
    return this;
  }

  /**
   * Get primaryButton
   * @return primaryButton
   */
  @NotNull @Valid 
  @Schema(name = "primaryButton", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("primaryButton")
  public FrontendButton getPrimaryButton() {
    return primaryButton;
  }

  public void setPrimaryButton(FrontendButton primaryButton) {
    this.primaryButton = primaryButton;
  }

  public FrontendError secondaryButton(@Nullable FrontendButton secondaryButton) {
    this.secondaryButton = secondaryButton;
    return this;
  }

  /**
   * Get secondaryButton
   * @return secondaryButton
   */
  @Valid 
  @Schema(name = "secondaryButton", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("secondaryButton")
  public @Nullable FrontendButton getSecondaryButton() {
    return secondaryButton;
  }

  public void setSecondaryButton(@Nullable FrontendButton secondaryButton) {
    this.secondaryButton = secondaryButton;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FrontendError frontendError = (FrontendError) o;
    return Objects.equals(this.code, frontendError.code) &&
        Objects.equals(this.title, frontendError.title) &&
        Objects.equals(this.description, frontendError.description) &&
        Objects.equals(this.primaryButton, frontendError.primaryButton) &&
        Objects.equals(this.secondaryButton, frontendError.secondaryButton);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, title, description, primaryButton, secondaryButton);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FrontendError {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    primaryButton: ").append(toIndentedString(primaryButton)).append("\n");
    sb.append("    secondaryButton: ").append(toIndentedString(secondaryButton)).append("\n");
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

