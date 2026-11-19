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
  - `infrastructure`: Adaptadores REST (`CheckoutController` + `GlobalExceptionHandler`), publicação/consumo assíncrono com RabbitMQ e DLQ, adapters simulados de catálogo e pagamento, cache Redis configurado em `CacheConfig`, observabilidade (`CorrelationIdFilter` + Micrometer) e persistência PostgreSQL via Spring Data JPA.
- **Virtual Threads (Java 21 / Project Loom):** Habilitadas em `spring.threads.virtual.enabled=true` no arquivo `src/main/resources/application.yaml` para operações bloqueantes de I/O.
- **Contrato Estável de Erros (`CheckoutErrorResponse`):** Falhas de negócio retornam uma estrutura JSON padronizada (`displayType`, `errorCode`, `cartId` e `frontendError`). O contrato contempla carrinho em processamento (`CHECKOUT_IN_PROGRESS`, HTTP 409).
- **Mensageria Orientada a Eventos (RabbitMQ):** A cada tentativa de checkout (aprovada ou recusada), um evento `CheckoutPaymentEvent` é publicado na exchange `checkout.events.exchange` e processado de forma assíncrona pela fila `checkout.payment.processed.queue` (com Dead Letter Queue `checkout.payment.processed.dlq` configurada para resiliência).
- **Proteção contra cobranças concorrentes:** Antes de chamar o gateway, o sistema faz uma atualização condicional no PostgreSQL para reservar o carrinho em `PROCESSING`. Apenas uma requisição consegue iniciar a cobrança; as concorrentes recebem HTTP 409. Carrinhos `DECLINED` podem iniciar uma nova tentativa. Se o gateway terminar com resultado incerto, o carrinho permanece `PROCESSING` e requer reconciliação antes de uma nova tentativa.
- **Minimização de dados de cartão:** O número completo e o CVV não são persistidos, registrados em log nem retornados; o recibo guarda somente os 4 últimos dígitos (`lastDigits`).
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
│   │   │   └── infrastructure/                    # Web, RabbitMQ, Redis, JPA e observabilidade
│   │   └── resources/
│   │       ├── openapi/checkout-bff-api.yaml      # Contrato OpenAPI 3.0 (API-First)
│   │       └── application.yaml                   # Banco, Redis, RabbitMQ, Actuator e virtual threads
│   └── test/java/com/dw/checkout/
│       ├── domain/service/                        # Testes unitários (JUnit 5/Mockito)
│       └── infrastructure/                       # Testes MVC e integração (Testcontainers)
├── src/main/java/com/dw/checkout/infrastructure/config/CacheConfig.java # TTLs e serialização Redis
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

```bash
./mvnw clean package -DskipTests
```

Suba todos os containers com build atualizado:

```bash
docker compose up -d --build
```

(Caso precise resetar o volume do banco de dados ou chaves antigas de cache entre versões, utilize docker compose down -v && docker compose up -d --build).

Acompanhe os logs de inicialização até o Spring Boot subir na porta 8080:

```bash
docker logs -f checkout-bff-app
```

### Valide a saúde de todos os serviços conectados:

```bash
curl -s http://localhost:8080/actuator/health | jq
```
O retorno deve indicar "status": "UP" para db (PostgreSQL), rabbit (RabbitMQ) e redis (Redis).

### Opção 2: Execução Local (IDE / Maven) + Infraestrutura em Docker
Ideal para desenvolvimento e depuração passo a passo no IntelliJ IDEA ou VS Code:

Suba apenas os serviços de apoio (PostgreSQL, RabbitMQ e Redis):

```bash
docker compose up -d postgres rabbitmq redis
```

Gere os fontes do contrato OpenAPI e execute a aplicação:

```bash
./mvnw clean spring-boot:run
```

## Endpoints e serviços locais

| Serviço | Endereço | Observações |
|---|---|---|
| Checkout BFF | `http://localhost:8080` | API REST |
| Pré-checkout | `POST /operations/pre-checkout` | Inicia um carrinho |
| Checkout | `POST /operations/checkout` | Processa o pagamento |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | Documentação interativa |
| Health | `http://localhost:8080/actuator/health` | Estado das dependências |
| Métricas de pagamento | `http://localhost:8080/actuator/metrics/checkout.payments.total` | Contadores Micrometer |
| RabbitMQ Management | `http://localhost:15672` | Usuário/senha local: `guest`/`guest` |
| PostgreSQL | `localhost:5432` | Banco `checkout_db`, usuário/senha `checkout`/`checkout` |
| Redis | `localhost:6379` | Cache dos adapters de catálogo e meios de pagamento |

A configuração de cache de produção fica em `src/main/java/com/dw/checkout/infrastructure/config/CacheConfig.java`: cache `products` com TTL de 15 minutos e `paymentMethods` com TTL de 30 minutos. O TTL padrão é 10 minutos. A desserialização polimórfica é restrita a tipos do domínio e coleções Java.

### Testes Automatizados e Cobertura (JaCoCo)
A suíte combina testes unitários de domínio (JUnit 5 + Mockito), testes da camada Web (`@WebMvcTest`) e testes de integração com PostgreSQL, Redis e RabbitMQ via Testcontainers. Os testes de integração são ignorados quando o Docker não está disponível; com Docker ativo, todos os containers são inicializados pelo Testcontainers.

### Executar toda a suíte de testes e gerar relatório de cobertura:

```bash
./mvnw clean test
```

### Abrir o relatório visual do JaCoCo no navegador (macOS / Linux):

```bash
open target/site/jacoco/index.html
```

Na última validação do projeto, `./mvnw clean test` concluiu com 18 testes, sem falhas ou erros. O total pode mudar conforme novos testes forem adicionados.

### Executar a Collection Automatizada no Postman:

Importe o arquivo docs/postman/checkout-bff-postman-collection.json no Postman.

Execute via Collection Runner para validar automaticamente os 9 cenários:
(pré-checkout, erro de produto inexistente, recusa cartão 0002, aprovação cartão 4242, bloqueio de duplicidade 409, pagamento Pix e métricas Actuator).

### Exemplos de Uso via cURL

### 1. Iniciar Pré-Checkout
Catálogo simulado disponível: 
basico (R$ 19,90), premium (R$ 39,90) e familia (R$ 59,90).

```bash
curl -i -X POST http://localhost:8080/operations/pre-checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-001" \
  -d '{"products": [{"slug": "premium"}]}'
```
### 2. Simular Recusa de Pagamento (Cartão final 0002)

```bash
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
```
### 3. Simular Checkout Aprovado com Cartão (Cartão final 4242)

```bash
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
```

### 4. Simular Checkout Aprovado com Pix

> **Nota:** Gere um novo `cartId` no passo 1 (um carrinho já aprovado retorna erro `409 Conflict`).

```bash
curl -i -X POST http://localhost:8080/operations/checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-004" \
  -d '{
    "cartId": "COLE_O_NOVO_CART_ID_AQUI",
    "paymentMethod": {
      "name": "Pix"
    }
  }'
```

### 5. Simular Checkout Aprovado com Cartão de Débito

```bash
CART_ID=$(curl -s -X POST http://localhost:8080/operations/pre-checkout \
  -H "Content-Type: application/json" \
  -d '{"products": [{"slug": "basico"}]}' | grep -oE '"cartId":"[^"]+"' | cut -d'"' -f4)

curl -i -X POST http://localhost:8080/operations/checkout \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: teste-curl-005" \
  -d "{
    \"cartId\": \"$CART_ID\",
    \"paymentMethod\": {
      \"name\": \"Cartão de Débito\",
      \"card\": {
        \"holderName\": \"Ana Souza\",
        \"number\": \"4111111111114242\",
        \"expiry\": \"12/28\",
        \"cvv\": \"123\"
      }
    }
  }"
```

