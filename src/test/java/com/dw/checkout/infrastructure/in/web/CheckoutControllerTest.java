package com.dw.checkout.infrastructure.in.web;

import com.dw.checkout.application.port.in.CreatePreCheckoutUseCase;
import com.dw.checkout.application.port.in.ProcessCheckoutUseCase;
import com.dw.checkout.CheckoutBffApplication;
import com.dw.checkout.domain.exception.CheckoutBusinessException;
import com.dw.checkout.domain.exception.ErrorCatalog;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.CartStatus;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.PaymentReceipt;
import com.dw.checkout.domain.model.Product;
import com.dw.checkout.infrastructure.config.CorrelationIdFilter;
import com.dw.checkout.infrastructure.observability.CheckoutMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CheckoutController.class)
@ContextConfiguration(classes = CheckoutBffApplication.class)
@Import({CheckoutWebMapper.class, GlobalExceptionHandler.class, CorrelationIdFilter.class})
class CheckoutControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatePreCheckoutUseCase createPreCheckoutUseCase;

    @MockitoBean
    private ProcessCheckoutUseCase processCheckoutUseCase;

    @MockitoBean
    private CheckoutMetrics checkoutMetrics;

    @Test
    @DisplayName("POST /operations/pre-checkout - Deve retornar 200 OK e propagar o X-Correlation-Id enviado")
    void shouldReturn200OnPreCheckoutAndPropagateCorrelationId() throws Exception {
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(
                cartId,
                List.of(new Product("premium", "Plano Premium", "Acesso completo em HD", new BigDecimal("39.90"), "BRL")),
                List.of(new PaymentMethod("Cartão de Crédito"), new PaymentMethod("Pix")),
                CartStatus.CREATED,
                null
        );

        when(createPreCheckoutUseCase.execute(List.of("premium"))).thenReturn(cart);

        mockMvc.perform(post("/operations/pre-checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "corr-test-123")
                        .content("""
                                {
                                  "products": [
                                    { "slug": "premium" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "corr-test-123"))
                .andExpect(jsonPath("$.cartId", is(cartId.toString())))
                .andExpect(jsonPath("$.products[0].slug", is("premium")))
                .andExpect(jsonPath("$.products[0].price", is(39.90)))
                .andExpect(jsonPath("$.paymentMethods[0].name", is("Cartão de Crédito")));
    }

    @Test
    @DisplayName("POST /operations/checkout - Deve retornar 200 APROVADO, mascarar cartão e gerar X-Correlation-Id automático")
    void shouldReturn200ApprovedAndMaskSensitiveCardData() throws Exception {
        UUID cartId = UUID.randomUUID();
        PaymentReceipt receipt = new PaymentReceipt(
                "Cartão de Crédito",
                "4242",
                null,
                new BigDecimal("39.90"),
                "BRL",
                Instant.parse("2026-11-18T18:00:00Z")
        );

        Cart approvedCart = new Cart(
                cartId,
                List.of(new Product("premium", "Plano Premium", "Acesso completo em HD", new BigDecimal("39.90"), "BRL")),
                List.of(new PaymentMethod("Cartão de Crédito")),
                CartStatus.APPROVED,
                receipt
        );

        when(processCheckoutUseCase.execute(any())).thenReturn(approvedCart);

        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": "%s",
                                  "paymentMethod": {
                                    "name": "Cartão de Crédito",
                                    "card": {
                                      "holderName": "Ana Souza",
                                      "number": "4111111111114242",
                                      "expiry": "12/28",
                                      "cvv": "999"
                                    }
                                  }
                                }
                                """.formatted(cartId)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", notNullValue()))
                .andExpect(jsonPath("$.cartId", is(cartId.toString())))
                .andExpect(jsonPath("$.status", is("APROVADO")))
                .andExpect(jsonPath("$.payment.lastDigits", is("4242")))
                .andExpect(content().string(not(containsString("4111111111114242"))))
                .andExpect(content().string(not(containsString("999"))));
    }

    @Test
    @DisplayName("POST /operations/checkout - Deve retornar 422 com contrato frontendError imutável quando pagamento for recusado")
    void shouldReturn422WithStandardizedFrontendErrorOnDeclinedPayment() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(processCheckoutUseCase.execute(any()))
                .thenThrow(new CheckoutBusinessException(ErrorCatalog.PAYMENT_DECLINED, cartId));

        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": "%s",
                                  "paymentMethod": {
                                    "name": "Cartão de Crédito",
                                    "card": {
                                      "holderName": "Ana Souza",
                                      "number": "4111111111110002",
                                      "expiry": "12/28",
                                      "cvv": "123"
                                    }
                                  }
                                }
                                """.formatted(cartId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.displayType", is("BLOCK")))
                .andExpect(jsonPath("$.errorCode", is("PAYMENT_DECLINED")))
                .andExpect(jsonPath("$.cartId", is(cartId.toString())))
                .andExpect(jsonPath("$.frontendError.code", is("P-1001")))
                .andExpect(jsonPath("$.frontendError.title", notNullValue()))
                .andExpect(jsonPath("$.frontendError.description", notNullValue()))
                .andExpect(jsonPath("$.frontendError.primaryButton.url", is("RETRY")))
                .andExpect(jsonPath("$.frontendError.secondaryButton.url", is("/checkout/pagamento")));
    }

    @Test
    @DisplayName("POST /operations/checkout - Deve retornar 409 Conflict quando carrinho já estiver aprovado")
    void shouldReturn409WhenCartAlreadyApproved() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(processCheckoutUseCase.execute(any()))
                .thenThrow(new CheckoutBusinessException(ErrorCatalog.CART_ALREADY_APPROVED, cartId));

        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": "%s",
                                  "paymentMethod": {
                                    "name": "Pix"
                                  }
                                }
                                """.formatted(cartId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.displayType", is("BLOCK")))
                .andExpect(jsonPath("$.errorCode", is("CART_ALREADY_APPROVED")))
                .andExpect(jsonPath("$.frontendError.code", is("P-1005")));
    }

    @Test
    @DisplayName("POST /operations/checkout - Deve manter formato frontendError mesmo em falha de Bean Validation (payload incompleto)")
    void shouldMaintainStandardizedErrorContractOnValidationFailure() throws Exception {
        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentMethod": {
                                    "name": ""
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.displayType", notNullValue()))
                .andExpect(jsonPath("$.errorCode", notNullValue()))
                .andExpect(jsonPath("$.frontendError.code", notNullValue()))
                .andExpect(jsonPath("$.frontendError.title", notNullValue()))
                .andExpect(jsonPath("$.frontendError.description", notNullValue()));
    }
}
