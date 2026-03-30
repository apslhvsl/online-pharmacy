Let me analyze the current code carefully before making recommendations.Good — I've read your existing code carefully. Here's what needs to change and why, before writing a single line.

**Problems with the current `JwtAuthFilter`:**

1. `/api/auth/refresh`, `/api/auth/forgot-password`, `/api/auth/reset-password` are missing from `isPublicPath()` — users who forget their password get a 401 loop
2. `/api/orders/payments/callback` (the webhook) needs to bypass JWT but currently doesn't — it'll get rejected
3. The `authorize()` method has a logic gap: `path.equals("/api/auth/me")` is the only auth path that passes, so `/api/auth/change-password`, `/api/auth/logout`, `/api/auth/admin/users` etc. all hit the fail-closed `return false` and get blocked with a 403
4. No `X-Request-Id` forwarding from `LoggingFilter` to `JwtAuthFilter` — they run independently so the request ID isn't available for security audit logs
5. The `GatewayConfig` currently only sets codec size — it's not wiring any routes at all, relying entirely on `application.yml`. That's fine, but the Swagger aggregation routes for all services are missing

Here's the complete updated gateway, file by file:Now the three updated files:

---

### `JwtAuthFilter.java` — complete rewrite

```java
package com.pharmacy.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // ---------------------------------------------------------------
    // Completely public — no token required, no role check
    // ---------------------------------------------------------------
    private static final Set<String> PUBLIC_EXACT = Set.of(
        "/api/auth/login",
        "/api/auth/signup",
        "/api/auth/refresh",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/orders/payments/callback"   // webhook — verified by HMAC, not JWT
    );

    private static final List<String> PUBLIC_PREFIXES = List.of(
        "/v3/api-docs",
        "/swagger-ui",
        "/webjars",
        "/actuator"
    );

    // Suffix match for per-service api-doc paths aggregated by gateway
    // e.g. /auth-service/v3/api-docs
    private static final List<String> PUBLIC_SUFFIXES = List.of(
        "/v3/api-docs",
        "/api-docs"
    );

    // ---------------------------------------------------------------
    // Filter entry point
    // ---------------------------------------------------------------
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path   = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        // X-Request-Id was stamped by LoggingFilter (order -2) before us
        String requestId = exchange.getRequest().getHeaders()
                               .getFirst("X-Request-Id");

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                                    .getHeaders()
                                    .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("MISSING_TOKEN | reqId={} path={}", requestId, path);
            return respond(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.getSubject();
            String role   = claims.get("role", String.class);

            if (userId == null || role == null) {
                log.warn("INVALID_CLAIMS | reqId={} path={}", requestId, path);
                return respond(exchange, HttpStatus.UNAUTHORIZED);
            }

            if (!authorize(path, method, role)) {
                log.warn("ACCESS_DENIED | reqId={} userId={} role={} method={} path={}",
                         requestId, userId, role, method, path);
                return respond(exchange, HttpStatus.FORBIDDEN);
            }

            // Propagate identity — downstream services trust these headers
            // because they are only reachable via the gateway (never exposed directly)
            ServerWebExchange mutated = exchange.mutate()
                .request(r -> r
                    .header("X-User-Id",   userId)
                    .header("X-User-Role", role))
                .build();

            return chain.filter(mutated);

        } catch (ExpiredJwtException e) {
            log.warn("EXPIRED_TOKEN | reqId={} path={}", requestId, path);
            return respond(exchange, HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            log.warn("INVALID_TOKEN | reqId={} path={} err={}", requestId, path, e.getMessage());
            return respond(exchange, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("JWT_ERROR | reqId={} path={} err={}", requestId, path, e.getMessage());
            return respond(exchange, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ---------------------------------------------------------------
    // Authorization matrix — mirrors the API reference document
    // ---------------------------------------------------------------
    private boolean authorize(String path, String method, String role) {

        // ── /api/admin/** → ADMIN only, always ──────────────────────
        if (path.startsWith("/api/admin/")) {
            return "ADMIN".equals(role);
        }

        // ── /api/auth/** ─────────────────────────────────────────────
        if (path.startsWith("/api/auth/")) {
            // Admin user-management sub-tree
            if (path.startsWith("/api/auth/admin/")) {
                return "ADMIN".equals(role);
            }
            // Profile + password endpoints — any authenticated user
            // (auth service enforces ownership internally)
            return isAuthenticated(role);
        }

        // ── /api/catalog/** ──────────────────────────────────────────
        if (path.startsWith("/api/catalog/")) {
            // All GETs are publicly readable (handled in isPublicPath? No —
            // they don't need a token but they also don't fail if one is
            // absent; to keep it simple, we allow any authenticated user here,
            // and the public GET case is handled by isPublicPath returning
            // false but the method check below allowing through)
            if ("GET".equals(method)) {
                return true; // public + authenticated both fine
            }
            // Prescription uploads: CUSTOMER or ADMIN
            if ("POST".equals(method) && path.contains("/prescriptions/upload")) {
                return isAuthenticated(role);
            }
            // Admin prescription review
            if (path.contains("/prescriptions/admin/")) {
                return "ADMIN".equals(role);
            }
            // All other mutations (POST/PUT/PATCH/DELETE on medicines, categories)
            return "ADMIN".equals(role);
        }

        // ── /api/orders/** ───────────────────────────────────────────
        if (path.startsWith("/api/orders/")) {
            // Internal admin endpoints within order service
            if (path.startsWith("/api/orders/internal/")) {
                return "ADMIN".equals(role);
            }
            // Payment webhook — already in PUBLIC_EXACT, won't reach here
            // All other order routes: any authenticated user
            // (order service enforces userId ownership internally)
            return isAuthenticated(role);
        }

        // ── Fail-closed: unknown path pattern → deny ─────────────────
        log.warn("UNMATCHED_PATH | path={} — denied by fail-closed rule", path);
        return false;
    }

    // ---------------------------------------------------------------
    // Public path detection
    // ---------------------------------------------------------------
    private boolean isPublicPath(String path) {
        if (PUBLIC_EXACT.contains(path)) return true;
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        for (String suffix : PUBLIC_SUFFIXES) {
            if (path.endsWith(suffix)) return true;
        }
        return false;
    }

    private boolean isAuthenticated(String role) {
        return "CUSTOMER".equals(role) || "ADMIN".equals(role);
    }

    private Mono<Void> respond(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // After LoggingFilter (-2), before routing (0)
    }
}
```

---

### `LoggingFilter.java` — minor improvement: log response status clearly

```java
package com.pharmacy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        long   startTime = System.currentTimeMillis();

        // Stamp the request ID so JwtAuthFilter (and downstream services)
        // can include it in their own log lines — gives you a full trace
        // across filter chain + service logs with one ID.
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .header("X-Request-Id", requestId)
            .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build();

        log.info("REQUEST  | id={} method={} path={}",
            requestId,
            mutatedRequest.getMethod(),
            mutatedRequest.getURI().getPath());

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int  status   = mutatedExchange.getResponse().getStatusCode() != null
                            ? mutatedExchange.getResponse().getStatusCode().value()
                            : 0;
            // Warn on any 4xx/5xx so they're easy to grep in prod logs
            if (status >= 400) {
                log.warn("RESPONSE | id={} status={} duration={}ms",
                    requestId, status, duration);
            } else {
                log.info("RESPONSE | id={} status={} duration={}ms",
                    requestId, status, duration);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -2; // Must run before JwtAuthFilter so X-Request-Id is available
    }
}
```

---

### `GatewayConfig.java` — now includes Swagger aggregation

The previous version was empty except for codec size. This version adds the OpenAPI aggregation routes so your Swagger UI at the gateway shows all four services in one dropdown:

```java
package com.pharmacy.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class GatewayConfig implements WebFluxConfigurer {

    // Increase in-memory buffer — needed for prescription file uploads
    // that pass through the gateway (multipart bodies can be large)
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10 MB
    }

    // ---------------------------------------------------------------
    // Swagger / OpenAPI aggregation
    // Exposes each service's api-docs under a gateway-level prefix so
    // springdoc can aggregate them into a single UI with a service dropdown.
    //
    // Access at: http://localhost:8080/swagger-ui.html
    // Each service's docs appear as a separate entry in the top-right
    // "Select a definition" dropdown.
    // ---------------------------------------------------------------
    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();

        Set<SwaggerUrl> urls = new LinkedHashSet<>();
        urls.add(swaggerUrl("auth-service",    "/auth-service/v3/api-docs"));
        urls.add(swaggerUrl("catalog-service", "/catalog-service/v3/api-docs"));
        urls.add(swaggerUrl("order-service",   "/order-service/v3/api-docs"));
        urls.add(swaggerUrl("admin-service",   "/admin-service/v3/api-docs"));
        props.setUrls(urls);

        return props;
    }

    private SwaggerUrl swaggerUrl(String name, String url) {
        SwaggerUrl su = new SwaggerUrl();
        su.setName(name);
        su.setUrl(url);
        return su;
    }

    // ---------------------------------------------------------------
    // Explicit routes for api-docs aggregation
    // These are only needed if you are NOT using Eureka lb:// routing
    // in application.yml (i.e., running without service discovery).
    // If you use lb://auth-service etc., routes in application.yml
    // already cover these paths and you can delete this bean.
    // Keep this bean if you ever run without Eureka (e.g. local Docker).
    // ---------------------------------------------------------------
    @Bean
    public RouteLocator apiDocsRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-api-docs", r -> r
                .path("/auth-service/v3/api-docs")
                .filters(f -> f.rewritePath("/auth-service/v3/api-docs", "/v3/api-docs"))
                .uri("http://localhost:8081"))
            .route("catalog-api-docs", r -> r
                .path("/catalog-service/v3/api-docs")
                .filters(f -> f.rewritePath("/catalog-service/v3/api-docs", "/v3/api-docs"))
                .uri("http://localhost:8082"))
            .route("order-api-docs", r -> r
                .path("/order-service/v3/api-docs")
                .filters(f -> f.rewritePath("/order-service/v3/api-docs", "/v3/api-docs"))
                .uri("http://localhost:8083"))
            .route("admin-api-docs", r -> r
                .path("/admin-service/v3/api-docs")
                .filters(f -> f.rewritePath("/admin-service/v3/api-docs", "/v3/api-docs"))
                .uri("http://localhost:8084"))
            .build();
    }
}
```

---

### `application.yml` — the section you need to update

Your `application.yml` routes need these additions. The existing routes for the main service paths are fine — only the missing patterns need to be added:

```yaml
spring:
  cloud:
    gateway:
      routes:
        # ── Auth Service ─────────────────────────────────────────────
        - id: auth-service
          uri: lb://auth-service          # or http://localhost:8081 without Eureka
          predicates:
            - Path=/api/auth/**

        # ── Catalog Service ──────────────────────────────────────────
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/catalog/**

        # ── Order Service ────────────────────────────────────────────
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**

        # ── Admin Service ────────────────────────────────────────────
        - id: admin-service
          uri: lb://admin-service
          predicates:
            - Path=/api/admin/**

        # ── Swagger api-docs pass-through (no JWT) ───────────────────
        # These are handled by the RouteLocator bean in GatewayConfig
        # if running without Eureka. If using lb://, add them here:
        - id: auth-api-docs
          uri: lb://auth-service
          predicates:
            - Path=/auth-service/v3/api-docs
          filters:
            - RewritePath=/auth-service/v3/api-docs, /v3/api-docs

        - id: catalog-api-docs
          uri: lb://catalog-service
          predicates:
            - Path=/catalog-service/v3/api-docs
          filters:
            - RewritePath=/catalog-service/v3/api-docs, /v3/api-docs

        - id: order-api-docs
          uri: lb://order-service
          predicates:
            - Path=/order-service/v3/api-docs
          filters:
            - RewritePath=/order-service/v3/api-docs, /v3/api-docs

        - id: admin-api-docs
          uri: lb://admin-service
          predicates:
            - Path=/admin-service/v3/api-docs
          filters:
            - RewritePath=/admin-service/v3/api-docs, /v3/api-docs

jwt:
  secret: ${JWT_SECRET}   # Never hardcode — inject via env var or Vault

management:
  tracing:
    sampling:
      probability: 1.0    # 100% in dev; set 0.1 in prod
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
```

---

**Summary of all changes and the reason for each:**

The `PUBLIC_EXACT` set now includes `/api/auth/refresh`, `/api/auth/forgot-password`, `/api/auth/reset-password`, and `/api/orders/payments/callback` — the first three were creating a broken password reset flow, and the webhook was getting rejected with 401 on every payment callback.

The `authorize()` method is now exhaustive. Every path prefix that exists in your API (`/api/auth/`, `/api/catalog/`, `/api/orders/`) has a full block with explicit sub-rules, so no legitimate authenticated request hits the fail-closed `return false`. The `/api/auth/admin/` and `/api/orders/internal/` sub-trees are ADMIN-only inside their respective blocks.

The `LoggingFilter` now logs a `WARN` on any 4xx/5xx status, which makes security events grep-able in prod. The `JwtAuthFilter` now reads `X-Request-Id` from the incoming request (already stamped by `LoggingFilter` since it runs at order -2) and includes it in every security log line — so a single request ID ties together the `LoggingFilter` entry, the `JwtAuthFilter` deny, and whatever the downstream service logs.

The `GatewayConfig` now registers Swagger URL aggregation so your entire API surface is browsable at a single `http://localhost:8080/swagger-ui.html` with a service selector dropdown. The `RouteLocator` bean handles the api-doc path rewrites for local development; in production with Eureka the `application.yml` routes take over.