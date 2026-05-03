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
@Slf4j //adds logging (log.info, log.warn, etc.)
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}") //Reads secret from application.yml
    private String jwtSecret;

    // paths that don't need a token at all
    private static final Set<String> PUBLIC_EXACT = Set.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/verify-otp",
            "/api/auth/resend-otp",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/orders/payments/callback"
    );

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars",
            "/actuator"
    );

    private static final List<String> PUBLIC_SUFFIXES = List.of(
            "/v3/api-docs",
            "/api-docs"
    );
    /*
    ServerWebExchange exchange - This represents the entire HTTP request + response From this we can access:Request (headers, body, path) Response (modify headers, status)
    void → no return value Mono<Void> → async version of void
    */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path      = exchange.getRequest().getURI().getPath();
        String method    = exchange.getRequest().getMethod().name();
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");

        // skip auth for CORS preflight requests A CORS preflight request is a safety check by the browser before sending certain cross-origin requests
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        // skip auth for public routes — but still strip any client-injected identity headers
        if (isPublicPath(path)) {
            ServerWebExchange sanitized = stripIdentityHeaders(exchange);
            return chain.filter(sanitized);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("MISSING_TOKEN | reqId={} path={}", requestId, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role   = claims.get("role", String.class);

            // both fields must be present in the token
            if (userId == null || role == null) {
                log.warn("INVALID_CLAIMS | reqId={} path={}", requestId, path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (!authorize(path, method, role)) {
                log.warn("ACCESS_DENIED | reqId={} userId={} role={} method={} path={}",
                        requestId, userId, role, method, path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // Strip any client-supplied identity headers first, then set gateway-owned values.
            // Without the remove step, .header() appends and a client could inject X-User-Role: ADMIN.
            ServerWebExchange mutated = stripIdentityHeaders(exchange).mutate()
                    .request(r -> r
                            .header("X-User-Id", userId)
                            .header("X-User-Role", role))
                    .build();

            return chain.filter(mutated);

        } catch (ExpiredJwtException e) {
            log.warn("EXPIRED_TOKEN | reqId={} path={}", requestId, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (JwtException e) {
            log.warn("INVALID_TOKEN | reqId={} path={} err={}", requestId, path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            log.error("JWT_ERROR | reqId={} path={} err={}", requestId, path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean authorize(String path, String method, String role) {
        if (path.startsWith("/api/admin/")) {
            return "ADMIN".equals(role);
        }
        if (path.startsWith("/api/auth/")) {
            if (path.startsWith("/api/auth/admin/")) {
                return "ADMIN".equals(role);
            }
            return isAuthenticated(role);
        }
        if (path.startsWith("/api/catalog/")) {
            if ("GET".equals(method)) return true;
            if (path.contains("/prescriptions/admin/")) return "ADMIN".equals(role);
            if ("POST".equals(method) && path.contains("/prescriptions/upload")) return isAuthenticated(role);
            if (path.contains("/prescriptions/") && !path.contains("/admin/")) return isAuthenticated(role);
            return "ADMIN".equals(role);
        }
        if (path.startsWith("/api/orders/") || path.equals("/api/orders")) {
            if (path.startsWith("/api/orders/internal/")) return "ADMIN".equals(role);
            return isAuthenticated(role);
        }
        log.warn("UNMATCHED_PATH | path={} denied", path);
        return false;
    }

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

    /** Remove any identity headers the client may have injected before we set our own. */
    private ServerWebExchange stripIdentityHeaders(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Role");
                    h.remove("X-User-Email");
                }))
                .build();
    }

    @Override
    public int getOrder() {
        return -1; // run after LoggingFilter (-2) but before everything else
    }
}
