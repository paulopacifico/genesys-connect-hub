# Genesys Connect Hub

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

A production-ready Spring Boot 3.3 integration hub for **Genesys Cloud**, built with Hexagonal Architecture (Ports & Adapters). Exposes a clean REST API for queues, agents, conversation metrics, webhooks, and health checks.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        HEXAGONAL ARCHITECTURE                       │
│                                                                     │
│   ┌──────────────┐         ┌──────────────────────────────────┐     │
│   │   REST API   │──────▶  │          INPUT PORTS             │     │
│   │ (Controllers)│  HTTP   │  QueueUseCase / AgentUseCase     │     │
│   └──────────────┘         │  MetricsUseCase / WebhookUseCase │     │
│                            └───────────────┬──────────────────┘     │
│   ┌──────────────┐                         │                        │
│   │  Swagger UI  │                ┌────────▼──────────┐             │
│   │  /swagger-ui │                │  APPLICATION      │             │
│   └──────────────┘                │  SERVICES (Core)  │             │
│                                   │  QueueService     │             │
│                             ┌─────┤  AgentService     ├─────┐       │
│                             │     │  MetricsService   │     │       │
│                        ┌────▼───┐ │  WebhookService   │ ┌───▼───┐   │
│                        │OUTPUT  │ └───────────────────┘ │OUTPUT │   │
│                        │PORTS   │                       │PORTS  │   │
│                        │        │                       │       │   │
│                   ┌────▼──────┐ │                   ┌───▼───────┐   │
│                   │ GENESYS   │ │                   │PERSISTENCE│   │
│                   │ ADAPTERS  │ │                   │ ADAPTERS  │   │
│                   │(SDK calls)│ │                   │(JPA/PGSQL)│   │
│                   └───────────┘ │                   └───────────┘   │
│                                 │                                   │
│              ┌──────────────────┴─────────────────────┐             │
│              │           DOMAIN MODEL                  │            │
│              │  Queue · Agent · ConversationMetric     │            │
│              │  WebhookEvent · ApiHealthStatus         │            │
│              └────────────────────────────────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
         │                                       │
   ┌─────▼──────┐                         ┌──────▼──────┐
   │ Genesys    │                         │ PostgreSQL  │
   │ Cloud API  │                         │     16      │
   └────────────┘                         └─────────────┘
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Docker | 24+ |
| Docker Compose | 2.20+ |
| PostgreSQL (local only) | 16 |
| Genesys Cloud account | — |

---

## Genesys Cloud Setup

### 1. Create a Genesys Developer Account
1. Go to [developer.genesys.cloud](https://developer.genesys.cloud)
2. Sign in with your Genesys Cloud credentials
3. Select your organization

### 2. Create an OAuth Client (Client Credentials)
1. Navigate to **Admin → Integrations → OAuth**
2. Click **Add Client**
3. Set **App Name**: `genesys-connect-hub`
4. Select **Grant Type**: `Client Credentials`
5. Under **Roles**, assign: `Analytics > Analytics Data Management`, `Routing > All Permissions`, `Users > View`
6. Click **Save** — copy the **Client ID** and **Client Secret**

### 3. Identify Your Region
Your region is the base URL of your Genesys Cloud org (e.g., `mypurecloud.com` for US East, `mypurecloud.de` for Frankfurt).
Full list: [Genesys Cloud Regions](https://developer.genesys.cloud/platform/api/)

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description | Example |
|----------|-------------|---------|
| `GENESYS_CLIENT_ID` | OAuth client ID from Genesys Cloud | `a1b2c3d4-...` |
| `GENESYS_CLIENT_SECRET` | OAuth client secret | `AbCdEfGh...` |
| `GENESYS_REGION` | Genesys Cloud API region hostname | `mypurecloud.com` |
| `DB_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/genesyshub` |
| `DB_USERNAME` | Database username | `genesys` |
| `DB_PASSWORD` | Database password | `genesys` |

---

## Running Locally

### 1. Start PostgreSQL
```bash
docker run -d \
  --name genesyshub-postgres \
  -e POSTGRES_DB=genesyshub \
  -e POSTGRES_USER=genesys \
  -e POSTGRES_PASSWORD=genesys \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

Or using Make:
```bash
make build
java -jar target/genesys-connect-hub-*.jar
```

---

## Running with Docker Compose

```bash
# Copy and configure environment
cp .env.example .env
# Edit .env with your Genesys credentials

# Build and start all services
make up

# Tail application logs
make logs

# Stop all services
make down
```

The app will be available at `http://localhost:8080`.

---

## API Documentation

Once running, open:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Available Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/queues` | List all queues |
| `GET` | `/api/v1/queues/{id}` | Get queue by ID |
| `GET` | `/api/v1/queues/media/{type}` | Filter queues by media type |
| `GET` | `/api/v1/agents/queue/{queueId}` | List agents in a queue |
| `GET` | `/api/v1/agents/{id}` | Get agent by ID |
| `GET` | `/api/v1/agents/queue/{queueId}/summary` | Agent status summary (grouped counts) |
| `GET` | `/api/v1/metrics/queue/{id}?from=&to=` | Conversation metrics for a queue |
| `GET` | `/api/v1/metrics/queue/{id}/summary?from=&to=` | Aggregated metrics summary |
| `GET` | `/api/v1/metrics/abandoned?from=&to=` | Abandoned calls in time range |
| `POST` | `/api/v1/webhooks/genesys` | Receive Genesys Cloud webhook event |
| `GET` | `/api/v1/health/genesys` | Genesys Cloud connectivity check |
| `GET` | `/actuator/health` | Spring Boot health check |
| `GET` | `/actuator/metrics` | Application metrics |

Date parameters (`from`, `to`) use ISO-8601 format: `2024-01-01T00:00:00Z`

---

## Running Tests

```bash
# All tests (unit + integration)
make test

# Unit tests only
mvn test -pl . -Dtest="**/*Test"

# Integration tests only (requires Docker for Testcontainers)
mvn test -Dtest="**/*IT"
```

Integration tests use **Testcontainers** — Docker must be running.

---

## Project Structure

```
src/
├── main/java/com/genesyshub/
│   ├── GenesysConnectHubApplication.java   # Entry point
│   ├── config/                             # Spring configuration classes
│   │   ├── AsyncConfig.java                # @EnableAsync
│   │   ├── GenesysClientConfig.java        # Genesys SDK beans + auth
│   │   ├── GenesysProperties.java          # @ConfigurationProperties
│   │   ├── JacksonConfig.java              # ObjectMapper with JavaTimeModule
│   │   └── OpenApiConfig.java              # Swagger / OpenAPI
│   ├── domain/
│   │   ├── model/                          # Immutable domain records + enums
│   │   └── port/
│   │       ├── in/                         # Input ports (use case interfaces)
│   │       └── out/                        # Output ports (repository/client interfaces)
│   ├── application/service/                # Use case implementations
│   └── infrastructure/
│       ├── genesys/                        # Genesys Cloud SDK adapters
│       ├── persistence/                    # JPA entities, repositories, adapters
│       │   ├── entity/
│       │   ├── mapper/
│       │   └── repository/
│       └── web/                            # REST controllers, DTOs, exception handler
│           ├── controller/
│           ├── dto/
│           └── mapper/
└── main/resources/
    ├── application.yml                     # Default + test + docker profiles
    ├── application-docker.yml              # Docker-specific overrides
    └── db/migration/                       # Flyway SQL migrations
        ├── V1__create_conversation_metrics.sql
        ├── V2__create_webhook_events.sql
        └── V3__add_indexes.sql
```

---

## Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.3 | Application framework |
| Java | 21 | Language (records, pattern matching, virtual threads) |
| Genesys Cloud SDK | 228.0.0 | Genesys Platform API client |
| Spring Data JPA | — | ORM / database access |
| PostgreSQL | 16 | Primary database |
| Flyway | — | Database migrations |
| MapStruct | 1.5.5 | Compile-time bean mapping |
| Lombok | — | Boilerplate reduction |
| springdoc-openapi | 2.5.0 | Swagger UI + OpenAPI spec |
| Testcontainers | 1.19.8 | Integration tests with real PostgreSQL |
| JUnit 5 + Mockito | — | Unit testing |
| AssertJ | — | Fluent test assertions |
| JaCoCo | 0.8.12 | Code coverage (85% gate on domain + application) |
| Docker Compose | — | Local orchestration |
