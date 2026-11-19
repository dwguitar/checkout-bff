package com.dw.checkout.infrastructure;


import com.dw.checkout.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CheckoutIntegrationTest extends AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Fluxo completo: Pré-checkout -> Recusa (0002) -> Sucesso (4242) -> Bloqueio duplicidade")
    void shouldExecuteFullCheckoutLifecycle() throws Exception {
        // 1. Pré-checkout
        String preCheckoutBody = """
                { "products": [{ "slug": "premium" }] }
                """;

        String preResponseStr = mockMvc.perform(post("/operations/pre-checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(preCheckoutBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").isNotEmpty())
                .andExpect(jsonPath("$.products[0].slug").value("premium"))
                .andExpect(jsonPath("$.paymentMethods.length()").value(3))
                .andReturn().getResponse().getContentAsString();

        JsonNode preJson = objectMapper.readTree(preResponseStr);
        String cartId = preJson.get("cartId").asText();

        // 2. Tentativa com cartão final 0002 -> HTTP 422 + frontendError
        String declinedBody = """
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
                """.formatted(cartId);

        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(declinedBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.displayType").value("BLOCK"))
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_DECLINED"))
                .andExpect(jsonPath("$.frontendError.code").value("P-1001"))
                .andExpect(jsonPath("$.frontendError.primaryButton.url").value("RETRY"));

        // 3. Tentativa com cartão válido final 4242 -> HTTP 200 APROVADO
        String approvedBody = """
                {
                  "cartId": "%s",
                  "paymentMethod": {
                    "name": "Cartão de Crédito",
                    "card": {
                      "holderName": "Ana Souza",
                      "number": "4111111111114242",
                      "expiry": "12/28",
                      "cvv": "123"
                    }
                  }
                }
                """.formatted(cartId);

        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approvedBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.payment.lastDigits").value("4242"))
                .andExpect(jsonPath("$.payment.pixCode").doesNotExist());

        // 4. Tentativa duplicada no mesmo carrinho -> HTTP 409
        mockMvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approvedBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CART_ALREADY_APPROVED"));
    }
}
