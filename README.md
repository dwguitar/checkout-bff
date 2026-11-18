# Checkout BFF — Assinaturas

Backend for Frontend (BFF) responsável por orquestrar o fluxo de pré-checkout e checkout de assinaturas, desenvolvido em **Java 21 + Spring Boot 3.4.3** seguindo **Arquitetura Hexagonal (Ports & Adapters)** e abordagem **API-First (OpenAPI 3.0)**.

---

## Visão Geral e Arquitetura

O projeto foi desenhado para desacoplar completamente as regras de negócio da camada de transporte HTTP, mensageria e persistência, garantindo previsibilidade de contrato para o frontend e facilidade de evolução.

### Pilares Técnicos
* **API-First (`src/main/resources/openapi/checkout-bff-api.yaml`):** O contrato OpenAPI 3.0 é a fonte única da verdade. Interfaces REST (`OperationsApi`) e DTOs são gerados automaticamente em tempo de compilação via `openapi-generator-maven-plugin`.
* **Arquitetura Hexagonal (Ports & Adapters):**
  * `domain`: Regras de negócio puras (sem dependências de Spring ou JPA), modelos imutáveis (`record`), eventos de domínio (`CheckoutPaymentEvent`) e catálogo centralizado de erros (`ErrorCatalog`).
  * `application`: Casos de uso (`CreatePreCheckoutUseCase`, `ProcessCheckoutUseCase`) e portas de saída (`ProductCatalogPort`, `PaymentMethodPort`, `PaymentGatewayPort`, `CartRepositoryPort`, `PaymentEventPublisherPort`).
  * `infrastructure`: Adaptadores de entrada REST (`CheckoutController` + `GlobalExceptionHandler`), mensageria assíncrona com RabbitMQ (`Producer` + `Consumer` com DLQ), clientes simulados desacoplados via interfaces com cache (`@Cacheable`), observabilidade (`CorrelationIdFilter` + `Micrometer`) e persistência em PostgreSQL (`Spring Data JPA`).
* **Contrato Estável de Erros (`CheckoutErrorResponse`):** Todas as falhas de negócio retornam HTTP `4xx` mantendo exatamente a mesma estrutura JSON (`displayType`, `errorCode`, `cartId` e `frontendError` com `code`, `title`, `description`, `primaryButton` e `secondaryButton`).
* **Mensageria Orientada a Eventos (RabbitMQ):** A cada tentativa de checkout (aprovada ou recusada), um evento `CheckoutPaymentEvent` é publicado na exchange `checkout.events.exchange` e processado de forma assíncrona pela fila `checkout.payment.processed.queue` (com Dead Letter Queue `checkout.payment.processed.dlq` configurada para resiliência).
* **Segurança e Conformidade (PCI-DSS):** O número completo do cartão e o CVV nunca são persistidos no banco de dados, logados ou retornados nas respostas — apenas os 4 últimos dígitos (`lastDigits`).
* **Observabilidade e Rastreabilidade:** Propagação automática de `X-Correlation-Id` (MDC nos logs e header HTTP de resposta), health checks detalhados e métricas customizadas de negócio (`checkout.payments.total` e `checkout.errors.total`).

---

## Estrutura do Projeto

```text
├── docs/
│   └── postman/
│       └── checkout-bff-postman-collection.json   # Collection automatizada com testes e variáveis
├── src/
│   ├── main/
│   │   ├── java/com/dw/checkout/
│   │   │   ├── application/                       # Portas de entrada (UseCases) e saída (Ports)
│   │   │   ├── domain/                            # Modelos, regras puras, eventos e ErrorCatalog
│   │   │   └── infrastructure/                    # Web (REST/Swagger), RabbitMQ, JPA, Clients, Observability
│   │   └── resources/
│   │       ├── openapi/checkout-bff-api.yaml      # Contrato OpenAPI 3.0 (API-First)
│   │       └── application.yml                    # Configurações de banco, RabbitMQ, cache e Actuator
│   └── test/                                      # Testes unitários (JUnit 5/Mockito) e integração (Testcontainers)
└── docker-compose.yml                             # Orquestração de PostgreSQL, RabbitMQ e Checkout BFF

```

## Exemplos de Uso via cURL
1. Iniciar Pré-Checkout
Catálogo simulado disponível: basico (R$ 19,90), premium (R$ 39,90) e familia (R$ 59,90).


curl -i -X POST http://localhost:8080/operations/pre-checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-001" \
  -d '{"products": [{"slug": "premium"}]}'

2. Simular Recusa de Pagamento (Cartão final 0002)


curl -i -X POST http://localhost:8080/operations/checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-002" \
  -d '{
    "cartId": "COLE_O_CART_ID_AQUI",
    "paymentMethod": {
      "name": "Cartão de Crédito",
      "card": {
        "holderName": "Ana Souza",
        "number": "4111111111110002",
        "expiry": "12/28",
        "cvv": "123"
      }
    }
  }'

3. Simular Checkout Aprovado com Cartão (Cartão final 4242)


curl -i -X POST http://localhost:8080/operations/checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-003" \
  -d '{
    "cartId": "COLE_O_CART_ID_AQUI",
    "paymentMethod": {
      "name": "Cartão de Crédito",
      "card": {
        "holderName": "Ana Souza",
        "number": "4111111111114242",
        "expiry": "12/28",
        "cvv": "123"
      }
    }
  }'


4. Simular Checkout Aprovado com Pix

curl -i -X POST http://localhost:8080/operations/checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-004" \
  -d '{
    "cartId": "COLE_O_NOVO_CART_ID_AQUI",
    "paymentMethod": {
      "name": "Pix"
    }
  }'
