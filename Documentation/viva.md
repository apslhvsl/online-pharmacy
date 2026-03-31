# Viva Preparation Notes — Pharmacy Microservices System

---

## 1. What is Microservices? How is it used in your project?

Microservices is an architectural style where an application is split into small, independent services, each responsible for a specific domain and communicating via APIs.

In this project, the system is divided into:
- `auth-service` (port 8081) — user identity, login, JWT
- `catalog-service` (port 8082) — medicines, inventory, prescriptions
- `order-service` (port 8083) — cart, checkout, payments
- `admin-service` (port 8084) — admin operations (no DB, orchestrates others)
- `notification-service` (port 8085) — email notifications via RabbitMQ
- `api-gateway` (port 8080) — single entry point, JWT validation, routing
- `eureka-server` (port 8761) — service registry

Each service has its own database (except admin-service), its own Dockerfile, and deploys independently.

---

## 2. Modular Design

Each service is internally layered:
- `controller` — handles HTTP requests
- `service` — business logic
- `repository` — database operations (JPA)
- `dto` — data transfer objects (decoupled from entities)
- `exception` — centralized error handling

Example: `catalog-service` has separate controllers for public access (`MedicineController`) and internal access (`InternalMedicineController`), keeping concerns cleanly separated.

---

## 3. REST API

All inter-client communication uses REST over HTTP with JSON.

Examples from this project:
- `POST /api/auth/login` — returns JWT tokens
- `GET /api/catalog/medicines` — paginated medicine list
- `POST /api/orders/checkout/confirm` — confirms an order
- `PATCH /api/admin/orders/{id}/status/{status}` — admin updates order status

HTTP methods used: GET (read), POST (create), PUT (full update), PATCH (partial update), DELETE (remove).

---

## 4. Validations

Spring Boot validation (`@Valid`, `@NotNull`, `@Email`, `@Size`) is used on request DTOs.

Examples:
- Signup: email format, password length validated before processing
- File upload in `catalog-service`: file type (PDF/JPG/PNG only) and size (max 5MB) validated in `PrescriptionService`
- Stock check: quantity must be positive before calling catalog-service

Validation failures are caught by `GlobalExceptionHandler` and returned as `400 Bad Request` with a meaningful message.

---

## 5. Error Handling

Every service has a `@RestControllerAdvice` class (`GlobalExceptionHandler`) that catches exceptions and returns structured JSON responses.

| Exception | Status | Example |
|---|---|---|
| `DuplicateEmailException` | 409 | Email already registered |
| `BadCredentialsException` | 401 | Wrong password |
| `EntityNotFoundException` | 404 | Medicine not found |
| `InsufficientStockException` | 409 | Not enough stock at checkout |
| `InvalidStateTransitionException` | 400 | Invalid order status change |
| `FeignException.NotFound` | 404 | Downstream service returned 404 |
| `FeignException` | 502 | Downstream service is down |

The `admin-service` specifically handles Feign errors and returns `502 Bad Gateway` when a downstream service fails.

---

## 6. Spring Cloud

Spring Cloud provides the infrastructure tools that make microservices work together:

| Component | Used In This Project |
|---|---|
| Eureka (Service Discovery) | All services register here; gateway resolves `lb://service-name` |
| Spring Cloud Gateway | `api-gateway` — routing, JWT auth, load balancing |
| OpenFeign | `admin-service`, `order-service` — HTTP calls between services |
| Micrometer + Zipkin | Distributed tracing across all services |

---

## 7. API Gateway

The `api-gateway` (port 8080) is the single entry point for all client requests.

What it does for every request:
1. `LoggingFilter` (order -2) — generates a `X-Request-Id` UUID, logs method + path
2. `JwtAuthFilter` (order -1) — validates Bearer token, checks role-based authorization
3. Routes to the correct service via Eureka (`lb://auth-service`, etc.)
4. Injects `X-User-Id` and `X-User-Role` headers so downstream services don't re-validate JWT

Public paths (no token needed): `/api/auth/login`, `/api/auth/signup`, `/api/auth/refresh`, `/api/orders/payments/callback`, Swagger UI, actuator endpoints.

Security: Internal paths like `/api/catalog/internal/**` return `404` at the routing level — they never reach the service even if someone bypasses the JWT filter.

---

## 8. Docker

Each service has a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

- `eclipse-temurin:21-jre-alpine` — Java 21 JRE on Alpine Linux (small image, JRE not JDK)
- `VOLUME /tmp` — Spring Boot uses `/tmp` for embedded Tomcat; declaring it improves I/O
- No shell wrapper in ENTRYPOINT — signals like `SIGTERM` go directly to the JVM for graceful shutdown

---

## 9. Docker Compose & Orchestration

`docker-compose.yml` orchestrates all services together:
- Starts PostgreSQL, RabbitMQ, Zipkin, Eureka, and all microservices
- Services communicate over a shared Docker network using service names (e.g., `http://eureka-server:8761`)
- Environment variables override `application.yaml` defaults (e.g., `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`, `JWT_SECRET`, `RABBITMQ_HOST`)
- `uploads-data` named volume persists prescription files across container restarts

Orchestration means Docker Compose manages starting, stopping, networking, and scaling of all containers automatically.

---

## 10. Layered Architecture

Every service follows the same 3-layer pattern:

```
Controller  →  receives HTTP request, calls service
Service     →  business logic, calls repository or Feign clients
Repository  →  Spring Data JPA, talks to PostgreSQL
```

Example in `order-service`:
- `CheckoutController` receives `POST /checkout/confirm`
- `CheckoutService` validates prescription, checks stock, deducts inventory, calculates pricing
- `OrderRepository` saves the final `Order` entity

---

## 11. Global Exception Handling

`@RestControllerAdvice` in each service catches all unhandled exceptions and returns consistent JSON error responses instead of stack traces.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
}
```

This means the client always gets a structured response, never a raw 500 with a stack trace.

---

## 12. Configuration Management

Application settings (DB URLs, ports, secrets) are stored in `application.yaml` per service, not hardcoded.

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/auth_db}
jwt:
  secret: ${JWT_SECRET:fallback-secret}
```

The `${VAR:default}` syntax reads from environment variables in production (Docker) and falls back to local defaults for development. The JWT secret is shared between `auth-service` (signs tokens) and `api-gateway` (verifies tokens) via the same `JWT_SECRET` environment variable.

---

## 13. Feign Client

Feign is used for synchronous REST communication between microservices. It turns an interface into an HTTP client automatically.

Example — `admin-service` calling `auth-service`:

```java
@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/api/auth/internal/users")
    PagedResponse<UserProfileResponse> listUsers(...);
}
```

`name = "auth-service"` — Feign resolves this to an actual IP/port via Eureka. No hardcoded URLs.

Used in:
- `admin-service` → calls auth, catalog, and order services
- `order-service` → calls catalog-service (stock checks, deductions) and auth-service (user info for notifications)

---

## 14. RabbitMQ

RabbitMQ is used for asynchronous, event-driven communication. Services publish events and move on — they don't wait for the notification to be sent.

Setup:
- Exchange: `pharmacy.notifications` (DirectExchange)
- Routing key `order.update` → `order.notification.queue`
- Routing key `password.reset` → `password.notification.queue`

Publishers:
- `order-service` publishes `OrderNotificationEvent` on every status change (`@Async` — runs in background thread)
- `auth-service` publishes `PasswordResetEvent` when forgot-password is triggered

Consumer:
- `notification-service` listens to both queues and sends HTML emails via SMTP2GO

Queues are **durable** — messages survive RabbitMQ restarts. If notification fails, it's logged but doesn't fail the original operation (notifications are non-critical).

---

## 15. Zipkin — Distributed Tracing

Zipkin runs on port `9411` and collects trace data from all services.

Every service has:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0   # trace 100% of requests
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

Every log line includes `traceId` and `spanId`:
```
INFO [api-gateway,abc123,def456] Incoming GET /api/orders/
```

To use the Zipkin dashboard:
1. Run Docker Compose
2. Open `http://localhost:9411`
3. Click "Run Query"
4. View the full request journey across all services with timing per service

This lets you debug slow requests and see exactly which service is the bottleneck.

---

## 16. Service Discovery — Eureka

Eureka Server (port 8761) is the service registry. Every service registers on startup.

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

When `api-gateway` routes to `lb://auth-service`, it asks Eureka for the current IP/port of `auth-service`. This means services can move, restart, or scale without any config changes.

The Eureka dashboard at `http://localhost:8761` shows all registered services and their health status.

---

## 17. JWT Authentication Flow

```
1. POST /api/auth/login → auth-service validates credentials
2. auth-service generates JWT (userId, email, role, 24h expiry, signed with HS256)
3. Client sends: Authorization: Bearer <token> on every request
4. api-gateway JwtAuthFilter validates token, extracts userId + role
5. Gateway injects X-User-Id and X-User-Role headers
6. Downstream services read these headers — no re-validation needed
```

Token rotation: when a refresh token is used, the old one is immediately revoked and a new pair is issued (prevents reuse attacks).

Password reset: always returns the same response whether the email exists or not — prevents email enumeration attacks.

---

## 18. Order State Machine

`order-service` uses a state machine to enforce valid order transitions. Every status change is validated before applying.

Key flow:
```
CHECKOUT_STARTED → PAYMENT_PENDING → PAID → PACKED → OUT_FOR_DELIVERY → DELIVERED
                                                                              ↓
                                                                    RETURN_REQUESTED → REFUND_INITIATED → REFUND_COMPLETED
```

Invalid transitions (e.g., DRAFT → DELIVERED) throw `InvalidStateTransitionException` (400 Bad Request).

---

## 19. Inventory — FEFO (First Expired, First Out)

`catalog-service` uses FEFO when selecting which batch to sell from:

```java
// Batches ordered by expiryDate ASC — earliest expiring first
Long bestBatchId = batches.get(0).getId();
```

This ensures older stock is sold before newer stock, reducing medicine waste. The `batchId` is stored in the cart so the same batch is deducted at checkout.

---

## 20. Admin Service — Orchestration Pattern

`admin-service` has no database. It's a thin orchestration layer that calls other services via Feign.

The dashboard aggregates data from multiple services:
```java
DashboardDto orderStats   = orderClient.getDashboard();       // from order-service
long lowStockCount        = catalogClient.getLowStockMedicines().size(); // from catalog-service
long pendingPrescriptions = catalogClient.getPendingQueue().getTotalElements(); // from catalog-service
```

This is the **Aggregator Pattern** — one service collects data from multiple sources and returns a unified response.

---

## Quick Reference — Ports

| Service | Port |
|---|---|
| api-gateway | 8080 |
| auth-service | 8081 |
| catalog-service | 8082 |
| order-service | 8083 |
| admin-service | 8084 |
| notification-service | 8085 |
| eureka-server | 8761 |
| zipkin | 9411 |
| rabbitmq management | 15672 |

---

## Final Summary (Say This in Viva)

"We built a microservices-based pharmacy system using Spring Boot and Spring Cloud. Services communicate synchronously via Feign clients and asynchronously via RabbitMQ. The API Gateway handles all routing, JWT validation, and role-based authorization. Service discovery is done through Eureka so no URLs are hardcoded. Distributed tracing is implemented with Zipkin. The entire system is containerized with Docker and orchestrated using Docker Compose."
