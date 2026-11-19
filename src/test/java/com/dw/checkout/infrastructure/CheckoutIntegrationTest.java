package com.dw.checkout.infrastructure;


import com.dw.checkout.AbstractIntegrationTest;
import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.domain.model.CartStatus;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.Product;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.in.RecoverStuckPaymentsUseCase;
import com.dw.checkout.infrastructure.adapter.in.messasing.RabbitMQPaymentConsumer;
import com.dw.checkout.infrastructure.adapter.out.persistence.CheckoutOutboxRepository;
import com.dw.checkout.infrastructure.adapter.out.persistence.ProcessedPaymentEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import io.micrometer.core.instrument.MeterRegistry;
import org.awaitility.Awaitility;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CheckoutIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CheckoutOutboxRepository outboxRepository;
    @Autowired private ProcessedPaymentEventRepository processedEventRepository;
    @Autowired private RabbitMQPaymentConsumer paymentConsumer;
    @Autowired private MeterRegistry meterRegistry;
    @Autowired private CartRepositoryPort cartRepository;
    @Autowired private RecoverStuckPaymentsUseCase recoveryUseCase;

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

        UUID completedCartId = UUID.fromString(cartId);
        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            var events = outboxRepository.findAllByCartIdOrderByCreatedAtAsc(completedCartId);
            assertEquals(2, events.size());
            assertTrue(events.stream().allMatch(row -> row.getPublishedAt() != null));
            assertEquals(2, events.stream().map(row -> row.getAttemptId()).distinct().count());
        });
    }

    @Test
    @DisplayName("Consumidor ignora eventos repetidos usando eventId")
    void shouldDeduplicateRepeatedPaymentEvents() {
        UUID eventId = UUID.randomUUID();
        CheckoutPaymentEvent event = new CheckoutPaymentEvent(eventId, UUID.randomUUID(), CartStatus.APPROVED,
                "Pix", new BigDecimal("39.90"), "BRL", List.of("premium"), java.time.Instant.now());
        double before = meterRegistry.counter("checkout.payments.total", "method", "Pix", "status", "APPROVED").count();

        paymentConsumer.onPaymentProcessed(event);
        paymentConsumer.onPaymentProcessed(event);

        assertTrue(processedEventRepository.existsById(eventId));
        assertEquals(before + 1, meterRegistry.counter(
                "checkout.payments.total", "method", "Pix", "status", "APPROVED").count());
    }

    @Test
    @DisplayName("Reconcilia carrinho antigo consultando o status do gateway")
    void shouldRecoverOldProcessingCartFromGatewayStatus() {
        UUID cartId = UUID.randomUUID();
        Product product = new Product("premium", "Plano Premium", "Assinatura mensal",
                new BigDecimal("39.90"), "BRL");
        Cart processing = new Cart(cartId, List.of(product), List.of(new PaymentMethod("Pix")),
                CartStatus.PROCESSING, null, "Pix", null,
                java.time.Instant.now().minus(Duration.ofMinutes(5)), UUID.randomUUID());
        cartRepository.save(processing);

        recoveryUseCase.recoverBefore(java.time.Instant.now().minus(Duration.ofMinutes(2)));

        Cart recovered = cartRepository.findById(cartId).orElseThrow();
        assertEquals(CartStatus.APPROVED, recovered.getStatus());
        assertTrue(recovered.getReceipt().pixCode().contains(cartId.toString()));
    }
}
