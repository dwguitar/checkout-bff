# Checkout BFF — Assinaturas

Backend for Frontend (BFF) responsável por orquestrar o fluxo de pré-checkout e checkout de assinaturas, desenvolvido em **Java 21 + Spring Boot 3.4.3** seguindo **Arquitetura Hexagonal (Ports & Adapters)** e abordagem **API-First (OpenAPI 3.0)**.

---

## Visão Geral e Arquitetura

O projeto foi desenhado para desacoplar completamente as regras de negócio da camada de transporte HTTP, mensageria e persistência, garantindo previsibilidade de contrato para o frontend e facilidade de evolução.

### Pilares Técnicos
- **API-First (`src/main/resources/openapi/checkout-bff-api.yaml`):** O contrato OpenAPI 3.0 é a fonte única da verdade. Interfaces REST (`OperationsApi`) e DTOs são gerados automaticamente em tempo de compilação via `openapi-generator-maven-plugin`.
- **Arquitetura Hexagonal (Ports & Adapters):**
  - `domain`: Regras de negócio puras (sem dependências de Spring ou JPA), modelos imutáveis (`record`), eventos de domínio (`CheckoutPaymentEvent`) e catálogo centralizado de erros (`ErrorCatalog`).
  - `application`: Casos de uso (`CreatePreCheckoutUseCase`, `ProcessCheckoutUseCase`) e portas de saída (`ProductCatalogPort`, `PaymentMethodPort`, `PaymentGatewayPort`, `CartRepositoryPort`, `PaymentEventPublisherPort`).
  - `infrastructure`: Adaptadores de entrada REST (`CheckoutController` + `GlobalExceptionHandler`), mensageria assíncrona com RabbitMQ (`Producer` + `Consumer` com DLQ), clientes simulados desacoplados via interfaces com cache distribuído em **Redis 7** (`@Cacheable` + `GenericJackson2JsonRedisSerializer`), observabilidade (`CorrelationIdFilter` + `Micrometer`) e persistência em PostgreSQL (`Spring Data JPA`).
- **Virtual Threads (Java 21 / Project Loom):** Habilitadas via `spring.threads.virtual.enabled=true` para maximizar a vazão de operações bloqueantes de I/O (orquestração de catálogo, cache Redis, gateway de pagamento, PostgreSQL e RabbitMQ) mantendo o código imperativo simples e legível.
- **Contrato Estável de Erros (`CheckoutErrorResponse`):** Todas as falhas de negócio retornam HTTP `4xx` mantendo exatamente a mesma estrutura JSON (`displayType`, `errorCode`, `cartId` e `frontendError` com `code`, `title`, `description`, `primaryButton` e `secondaryButton`).
- **Mensageria Orientada a Eventos (RabbitMQ):** A cada tentativa de checkout (aprovada ou recusada), um evento `CheckoutPaymentEvent` é publicado na exchange `checkout.events.exchange` e processado de forma assíncrona pela fila `checkout.payment.processed.queue` (com Dead Letter Queue `checkout.payment.processed.dlq` configurada para resiliência).
- **Segurança e Conformidade (PCI-DSS):** O número completo do cartão e o CVV nunca são persistidos no banco de dados, logados ou retornados nas respostas — apenas os 4 últimos dígitos (`lastDigits`).
- **Observabilidade e Rastreabilidade:** Propagação automática de `X-Correlation-Id` (MDC nos logs e header HTTP de resposta), health checks detalhados e métricas customizadas de negócio (`checkout.payments.total` e `checkout.errors.total`).

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
│   │   │   └── infrastructure/                    # Web (REST/Swagger), RabbitMQ, Redis, JPA, Observability
│   │   └── resources/
│   │       ├── openapi/checkout-bff-api.yaml      # Contrato OpenAPI 3.0 (API-First)
│   │       └── application.yml                    # Configurações de banco, Redis, RabbitMQ e Actuator
│   └── test/                                      # Testes unitários (JUnit 5/Mockito) e integração (Testcontainers)
├── Dockerfile                                     # Build da imagem da aplicação Java 21
├── docker-compose.yml                             # Orquestração de PostgreSQL, RabbitMQ, Redis e Checkout BFF
└── pom.xml                                        # Dependências, OpenAPI Generator e JaCoCo
```


## Como Rodar a Aplicação (Passo a Passo)
### Pré-requisitos
### Docker e Docker Compose em execução na máquina

Java 21 (JDK 21) e Maven 3.9+ (ou utilizar o wrapper ./mvnw incluído no repositório)

Opção 1: Execução Completa via Docker Compose (Recomendado)
Esta opção sobe toda a infraestrutura (PostgreSQL 16, RabbitMQ 3.13, Redis 7) e a aplicação Checkout BFF já configurada.

Compile o projeto e gere o pacote .jar (opcional caso o Dockerfile realize build multi-stage, mas recomendado para garantir o artefato atualizado):

./mvnw clean package -DskipTests

Suba todos os containers com build atualizado:
docker compose up -d --build

(Caso precise resetar o volume do banco de dados ou chaves antigas de cache entre versões, utilize docker compose down -v && docker compose up -d --build).

Acompanhe os logs de inicialização até o Spring Boot subir na porta 8080:
docker logs -f checkout-bff-app

Valide a saúde de todos os serviços conectados:

curl -s http://localhost:8080/actuator/health | jq

O retorno deve indicar "status": "UP" para db (PostgreSQL), rabbit (RabbitMQ) e redis (Redis).

Opção 2: Execução Local (IDE / Maven) + Infraestrutura em Docker
Ideal para desenvolvimento e depuração passo a passo no IntelliJ IDEA ou VS Code:

Suba apenas os serviços de apoio (PostgreSQL, RabbitMQ e Redis):

docker compose up -d postgres rabbitmq redis

Gere os fontes do contrato OpenAPI e execute a aplicação:
./mvnw clean spring-boot:run

Endpoints Úteis e Interfaces de ApoioServiço / FerramentaURL / PortaCredenciais / ObservaçõesCheckout BFF APIhttp://localhost:8080Endpoints /operations/pre-checkout e /operations/checkoutSwagger UI (OpenAPI)http://localhost:8080/swagger-ui.htmlDocumentação interativa gerada a partir do YAMLActuator Healthhttp://localhost:8080/actuator/healthStatus integrado de PostgreSQL, RabbitMQ e RedisMétricas Micrometerhttp://localhost:8080/actuator/metrics/checkout.payments.totalContadores de pagamentos aprovados e recusadosRabbitMQ Managementhttp://localhost:15672Usuário: guest | Senha: guestPostgreSQL 16localhost:5432Banco: checkout_db | Usuário: checkout | Senha: checkoutRedis 7 (Cache)localhost:6379Inspeção rápida: docker exec -it checkout-redis redis-cli KEYS "*"


Testes Automatizados e Cobertura (JaCoCo)
A suíte combina testes unitários de domínio (JUnit 5 + Mockito), testes da camada Web (@WebMvcTest) e testes de integração ponta a ponta com banco real via Testcontainers (certifique-se de que o Docker esteja ativo antes de rodar):

Executar toda a suíte de testes e gerar relatório de cobertura:
./mvnw clean test

Abrir o relatório visual do JaCoCo no navegador (macOS / Linux):
open target/site/jacoco/index.html

Executar a Collection Automatizada no Postman:

Importe o arquivo docs/postman/checkout-bff-postman-collection.json no Postman.

Execute via Collection Runner para validar automaticamente os 9 cenários (pré-checkout, erro de produto inexistente, recusa cartão 0002, aprovação cartão 4242, bloqueio de duplicidade 409, pagamento Pix e métricas Actuator).

Exemplos de Uso via cURL
### 1. Iniciar Pré-Checkout
Catálogo simulado disponível: basico (R$ 19,90), premium (R$ 39,90) e familia (R$ 59,90).
curl -i -X POST http://localhost:8080/operations/pre-checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-001" \
  -d '{"products": [{"slug": "premium"}]}'

### 2. Simular Recusa de Pagamento (Cartão final 0002)
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

### 3. Simular Checkout Aprovado com Cartão (Cartão final 4242)

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


### 4. Simular Checkout Aprovado com Pix
curl -i -X POST http://localhost:8080/operations/checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-004" \
  -d '{
    "cartId": "COLE_O_NOVO_CART_ID_AQUI",
    "paymentMethod": {
      "name": "Pix"
    }
  }'


