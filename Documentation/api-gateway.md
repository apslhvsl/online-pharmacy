# API Gateway — Deep Dive Documentation

This document covers every file in the `api-gateway` module, what it does, why it exists, and how it fits into the overall pharmacy microservices system. Use this as your reference for the evaluation.

---

## What is the API Gateway?

The API Gateway is the **single entry point** for all client requests. Instead of clients talking directly to auth-service, catalog-service, order-service, or admin-service, every request hits port `8080` on the gateway first. The gateway then:

1. Logs the request
2. Validates the JWT token
3. Checks authorization (role-based)
4. Routes the request to the correct downstream service
5. Logs the response

This pattern is called the **Gateway Pattern** in microservices architecture.

---

## File-by-File Breakdown

---

### `ApiGatewayApplication.java`

```
api-gateway/src/main/java/com/pharmacy/gateway/ApiGatewayApplication.java
```

The entry point of the Spring Boot application. The `@SpringBootApplication` annotation bootstraps the entire Spring context — it enables component scanning, auto-configuration, and configuration properties. When you run the JAR, `main()` is the first method called.

Nothing custom here — it's intentionally minimal. All the real logic lives in the filters and config.

---

### `config/GatewayConfig.java`

```
api-gateway/src/main/java/com/pharmacy/gateway/config/GatewayConfig.java
```

A Spring `@Configuration` class that handles two responsibilities:

**1. Buffer size for large uploads**

```java
configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10 MB
```

The gateway uses Spring WebFlux (reactive/non-blocking). By default, WebFlux buffers up to 256KB in memory per request. Prescription uploads (PDF/image files) can be much larger, so this raises the limit to 10MB. Without this, large file uploads would fail with a `DataBufferLimitException`.

**2. Swagger/OpenAPI aggregation**

```java
urls.add(swaggerUrl("auth-service",    "/auth-service/v3/api-docs"));
urls.add(swaggerUrl("catalog-service", "/catalog-service/v3/api-docs"));
urls.add(swaggerUrl("order-service",   "/order-service/v3/api-docs"));
urls.add(swaggerUrl("admin-service",   "/admin-service/v3/api-docs"));
```

Instead of visiting each service's Swagger UI separately, this config aggregates all four services' API docs into a single UI at `http://localhost:8080/swagger-ui.html`. The `@Primary` annotation ensures this bean overrides any auto-configured default. The `@Bean` method returns a `SwaggerUiConfigProperties` object that tells springdoc where to fetch each service's OpenAPI spec.

---

### `filter/LoggingFilter.java`

```
api-gateway/src/main/java/com/pharmacy/gateway/filter/LoggingFilter.java
```

A **GlobalFilter** — meaning it runs on every single request passing through the gateway. It runs at order `-2`, which means it executes before `JwtAuthFilter` (order `-1`). This is intentional.

**What it does:**

1. Generates a unique `UUID` as a `requestId` for every incoming request
2. Stamps that ID onto the request as the `X-Request-Id` header
3. Logs the method and path on the way in
4. After the downstream response comes back, logs the status code and how long it took (in ms)
5. If the status is 4xx or 5xx, it logs at `WARN` level instead of `INFO` — making security events easy to grep in production logs

**Why order `-2`?**

The `X-Request-Id` header must be added before `JwtAuthFilter` runs, because `JwtAuthFilter` reads that header to include it in its own warning/error log lines. This gives you a single trace ID you can grep across all log lines for one request.

```java
@Override
public int getOrder() {
    return -2; // Must run before JwtAuthFilter
}
```

---

### `filter/JwtAuthFilter.java`

```
api-gateway/src/main/java/com/pharmacy/gateway/filter/JwtAuthFilter.java
```

The most important file in the gateway. This is a **GlobalFilter** at order `-1` that handles authentication and authorization for every request.

**Step 1 — Public path bypass**

Some paths don't require a token (login, signup, Swagger UI, actuator health checks, payment callbacks). These are defined in two sets:

```java
private static final Set<String> PUBLIC_EXACT = Set.of(
    "/api/auth/login",
    "/api/auth/signup",
    "/api/auth/refresh",
    "/api/auth/forgot-password",
    "/api/auth/reset-password",
    "/api/orders/payments/callback"
);

private static final List<String> PUBLIC_PREFIXES = List.of(
    "/v3/api-docs", "/swagger-ui", "/webjars", "/actuator"
);
```

If the path matches, the filter calls `chain.filter(exchange)` immediately — no token check.

**Step 2 — Token extraction**

Checks for the `Authorization: Bearer <token>` header. If missing or malformed, returns `401 Unauthorized`.

**Step 3 — JWT validation**

Parses and verifies the token using the shared `JWT_SECRET` (HMAC-SHA256). Extracts `userId` (the `sub` claim) and `role`. If either is missing, returns `401`.

**Step 4 — Role-based authorization**

The `authorize()` method enforces access rules per path:

| Path prefix | Rule |
|---|---|
| `/api/admin/**` | ADMIN only |
| `/api/auth/admin/**` | ADMIN only |
| `/api/auth/**` | Any authenticated user |
| `/api/catalog/**` GET | Public (no auth needed) |
| `/api/catalog/**/prescriptions/upload` | Any authenticated user |
| `/api/catalog/**/prescriptions/admin/**` | ADMIN only |
| `/api/catalog/**` (write) | ADMIN only |
| `/api/orders/internal/**` | ADMIN only |
| `/api/orders/**` | Any authenticated user |

If the role doesn't match, returns `403 Forbidden`.

**Step 5 — Header injection**

On success, the filter mutates the request to add two headers before forwarding downstream:

```java
.header("X-User-Id", userId)
.header("X-User-Role", role)
```

Downstream services (order-service, catalog-service, etc.) trust these headers to know who is making the request — they don't need to re-validate the JWT themselves.

**Error handling**

| Exception | Response |
|---|---|
| `ExpiredJwtException` | 401 — token expired |
| `JwtException` | 401 — token invalid/tampered |
| Any other exception | 500 — internal error |

---

### `resources/application.yml`

```
api-gateway/src/main/resources/application.yml
```

The main configuration file. Here's what each section does:

**Server**
```yaml
server:
  port: 8080
```
The gateway listens on port 8080. All external traffic goes here.

**Routes**

Each route has three parts: a `predicate` (what path to match), a `uri` (where to send it), and optional `filters` (transformations).

- `lb://auth-service` — `lb://` means load-balanced via Eureka. The gateway asks Eureka for the actual IP/port of `auth-service` at runtime.
- `PreserveHostHeader` — keeps the original `Host` header so downstream services see the real host.

**Internal path blocking**

```yaml
- id: catalog-internal-block
  uri: no://op
  predicates:
    - Path=/api/catalog/internal/**
  filters:
    - SetStatus=404
```

Paths like `/api/catalog/internal/**` are meant for service-to-service calls only (not external clients). These routes return `404` immediately — the request never reaches the service. This is a security measure.

**Swagger aggregation routes**

```yaml
- id: auth-api-docs
  uri: lb://auth-service
  predicates:
    - Path=/auth-service/v3/api-docs
  filters:
    - RewritePath=/auth-service/v3/api-docs, /v3/api-docs
```

When the Swagger UI requests `/auth-service/v3/api-docs`, the gateway rewrites the path to `/v3/api-docs` and forwards it to auth-service. This is how the aggregated Swagger UI works.

**HTTP client timeouts**
```yaml
httpclient:
  connect-timeout: 5000   # 5 seconds to establish connection
  response-timeout: 5s    # 5 seconds to wait for response
```

**Eureka**
```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}
```
Registers the gateway with Eureka and uses it for service discovery. The `${VAR:default}` syntax means it reads from an environment variable, falling back to localhost for local development.

**JWT secret**
```yaml
jwt:
  secret: ${JWT_SECRET:...}
```
Injected from environment variable in production. The fallback value is used locally.

**Distributed tracing**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: ${MANAGEMENT_ZIPKIN_TRACING_ENDPOINT:http://localhost:9411/api/v2/spans}
```
Every request is traced (100% sampling) and sent to Zipkin. This lets you visualize the full journey of a request across all services.

**Actuator endpoints**
```yaml
endpoints:
  web:
    exposure:
      include: health,info,metrics,prometheus
```
Exposes `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus` for monitoring. These are in the public path list in `JwtAuthFilter` so they don't require a token.

**Logging pattern**
```yaml
pattern:
  level: "%5p [${spring.application.name},%X{traceId},%X{spanId}]"
```
Every log line includes the Zipkin `traceId` and `spanId`, so you can correlate gateway logs with downstream service logs in a tracing tool.

---

### `pom.xml`

The Maven build file. Key dependencies:

| Dependency | Purpose |
|---|---|
| `spring-cloud-starter-gateway` | The core gateway engine (WebFlux-based reactive routing) |
| `spring-cloud-starter-netflix-eureka-client` | Service discovery — resolves `lb://service-name` to real addresses |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | JWT parsing and validation |
| `springdoc-openapi-starter-webflux-ui` | Swagger UI + OpenAPI aggregation (WebFlux version) |
| `lombok` | Reduces boilerplate (`@Slf4j` for logging in `JwtAuthFilter`) |
| `micrometer-tracing-bridge-brave` | Distributed tracing instrumentation |
| `zipkin-reporter-brave` | Sends trace data to Zipkin |
| `spring-boot-starter-actuator` | Health checks and metrics endpoints |

The parent POM is `pharmacy-backend` — this means shared dependency versions (Spring Boot, Spring Cloud, etc.) are managed centrally and not repeated here.

Java version is set to 21 (LTS).

---

### `Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

A minimal production Docker image:

- `eclipse-temurin:21-jre-alpine` — Java 21 JRE on Alpine Linux. JRE (not JDK) because you only need to run the JAR, not compile. Alpine keeps the image small.
- `VOLUME /tmp` — Spring Boot uses `/tmp` for embedded Tomcat's working directory. Declaring it as a volume improves I/O performance in Docker.
- `COPY target/*.jar app.jar` — copies the built fat JAR into the image.
- `ENTRYPOINT` — runs the JAR directly. No shell wrapper, so signals (like `SIGTERM` from Docker stop) go straight to the JVM for graceful shutdown.

---

## Request Flow Summary

```
Client
  │
  ▼
LoggingFilter (order -2)
  │  → generates X-Request-Id
  │  → logs method + path
  │
  ▼
JwtAuthFilter (order -1)
  │  → checks if path is public
  │  → validates Bearer token
  │  → checks role authorization
  │  → injects X-User-Id, X-User-Role headers
  │
  ▼
Spring Cloud Gateway Router
  │  → matches route by path predicate
  │  → resolves lb://service-name via Eureka
  │
  ▼
Downstream Service (auth / catalog / order / admin)
  │
  ▼
LoggingFilter (response callback)
  │  → logs status + duration
  │
  ▼
Client receives response
```

---

## Security Design Decisions Worth Knowing

- Internal paths (`/api/*/internal/**`) are blocked at the routing level with `SetStatus=404` — even if someone bypasses the JWT filter somehow, the route goes nowhere.
- The JWT secret is never hardcoded in production — it's always injected via the `JWT_SECRET` environment variable.
- Downstream services never re-validate JWTs. They trust the `X-User-Id` and `X-User-Role` headers injected by the gateway. This means the gateway is the single security boundary — if it's compromised, all services are.
- The payment callback (`/api/orders/payments/callback`) is intentionally public because it's called by the payment provider (external system), not by a logged-in user.
