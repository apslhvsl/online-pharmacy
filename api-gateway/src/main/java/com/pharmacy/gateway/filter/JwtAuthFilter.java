//package com.pharmacy.gateway.filter;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@Component
//public class JwtAuthFilter implements GlobalFilter, Ordered {
//
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//
//    // These paths are open — no JWT required
//    private static final List<String> PUBLIC_PATHS = List.of(
//            "/api/auth/signup",
//            "/api/auth/login",
//            "/api/auth/api-docs",        // Add this
//            "/api/catalog/v3/api-docs",  // Add this
//            "/api/orders/v3/api-docs",   // Add this
//            "/api/admin/api-docs",       // Add this
//            "/v3/api-docs",              // Gateway's own docs
//            "/swagger-ui.html",          // Gateway's UI
//            "/swagger-ui/index.html"     // Gateway's UI resources
//    );
//
////    @Override
////    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
////        String path = exchange.getRequest().getURI().getPath();
////
////        // Skip JWT check for public paths
////        if (isPublicPath(path)) {
////            return chain.filter(exchange);
////        }
////
////        // Get Authorization header
////        String authHeader = exchange.getRequest()
////                .getHeaders()
////                .getFirst(HttpHeaders.AUTHORIZATION);
////
////        // Missing or malformed header
////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////            return unauthorized(exchange);
////        }
////
////        String token = authHeader.substring(7);
////
////        try {
////            Claims claims = Jwts.parser()
////                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
////                    .build()
////                    .parseSignedClaims(token)
////                    .getPayload();
////
////            String userId = claims.getSubject();
////            String role = claims.get("role", String.class);
////
////            // Block non-admins from admin routes
////            if (path.startsWith("/api/admin/") && !"ADMIN".equals(role)) {
////                return forbidden(exchange);
////            }
////
////            // Forward user context to downstream services
////            ServerWebExchange mutatedExchange = exchange.mutate()
////                    .request(r -> r.header("X-User-Id", userId)
////                            .header("X-User-Role", role))
////                    .build();
////
////            return chain.filter(mutatedExchange);
////
////        } catch (Exception e) {
////            return unauthorized(exchange);
////        }
////    }
//@Override
//public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//    String path = exchange.getRequest().getURI().getPath();
//    String method = exchange.getRequest().getMethod().name(); // Get HTTP Method (GET, POST, etc.)
//
//    if (isPublicPath(path)) {
//        return chain.filter(exchange);
//    }
//
//    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//        return unauthorized(exchange);
//    }
//
//    String token = authHeader.substring(7);
//
//    try {
//        Claims claims = Jwts.parser()
//                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//
//        String userId = claims.getSubject();
//        String role = claims.get("role", String.class);
//
//        // --- NEW SECURITY LOGIC START ---
//
//        // 1. Strict Admin Route Check
//        if (path.startsWith("/api/admin/") && !"ADMIN".equals(role)) {
//            return forbidden(exchange);
//        }
//
//        // 2. Catalog Service Protection
//        // Block Customers from POST/PUT/DELETE on medicines,
//        // but ALLOW them to POST prescriptions.
//        if (path.startsWith("/api/catalog/") && !"ADMIN".equals(role)) {
//            if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
//                // Exception: Customers MUST be allowed to upload prescriptions
//                if (!path.endsWith("/prescriptions/upload")) {
//                    return forbidden(exchange);
//                }
//            }
//        }
//
//        // --- NEW SECURITY LOGIC END ---
//
//        ServerWebExchange mutatedExchange = exchange.mutate()
//                .request(r -> r.header("X-User-Id", userId)
//                        .header("X-User-Role", role))
//                .build();
//
//        return chain.filter(mutatedExchange);
//
//    } catch (Exception e) {
//        return unauthorized(exchange);
//    }
//}
//    private boolean isPublicPath(String path) {
//        return path.equals("/api/auth/login") ||
//                path.equals("/api/auth/signup") ||
//
//                // Swagger (strict)
//                path.startsWith("/v3/api-docs") ||
//                path.startsWith("/swagger-ui") ||
//
//                // Service docs (strict prefixes)
//                path.startsWith("/api/auth/api-docs") ||
//                path.startsWith("/api/catalog/v3/api-docs") ||
//                path.startsWith("/api/orders/v3/api-docs") ||
//                path.startsWith("/api/admin/api-docs");
//    }
//
////    private boolean isPublicPath(String path) {
////        return PUBLIC_PATHS.stream().anyMatch(path::equals);
////    }
//
//
//    private Mono<Void> unauthorized(ServerWebExchange exchange) {
//        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//        return exchange.getResponse().setComplete();
//    }
//
//    private Mono<Void> forbidden(ServerWebExchange exchange) {
//        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//        return exchange.getResponse().setComplete();
//    }
//
//    @Override
//    public int getOrder() {
//        return -1; // Run before all other filters
//    }
//}



package com.pharmacy.gateway.filter;

import io.jsonwebtoken.Claims;
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

@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // 1. Public endpoints bypass
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 2. Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            // 3. Parse JWT
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 4. Validate claims existence
            if (userId == null || role == null) {
                return unauthorized(exchange);
            }

            // 5. Hardened Authorization Logic
            if (!authorize(path, method, role)) {
                log.warn("Access Denied: User={} Role={} Method={} Path={}", userId, role, method, path);
                return forbidden(exchange);
            }

            // 6. Propagate identity to downstream services
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", userId)
                            .header("X-User-Role", role))
                    .build();

            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    private boolean authorize(String path, String method, String role) {
        // ADMIN routes: Strict check
        if (path.startsWith("/api/admin/")) {
            return "ADMIN".equals(role);
        }

        // CATALOG rules: Granular access
        if (path.startsWith("/api/catalog/")) {
            if (method.equals("GET")) return true; // Publicly readable

            // Prescription uploads: Allowed for CUSTOMER and ADMIN
            if (method.equals("POST") && path.endsWith("/prescriptions/upload")) {
                return true;
            }

            // Catalog modifications: Block PATCH, POST, PUT, DELETE for non-admins
            if (method.matches("POST|PUT|PATCH|DELETE")) {
                return "ADMIN".equals(role);
            }
            return false;
        }

        // ORDERS: Any authenticated user (Service handles ownership)
        if (path.startsWith("/api/orders/")) {
            return true;
        }

        // AUTH: Allow specific internal profile endpoints
        if (path.equals("/api/auth/me")) {
            return true;
        }

        // FAIL-CLOSED: If no rule matches, block the request
        return false;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/signup") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars") ||
                // API doc paths — both /v3/api-docs and /api-docs variants
                path.endsWith("/v3/api-docs") ||
                path.endsWith("/api-docs") ||
                path.contains("/v3/api-docs/") ||
                path.startsWith("/swagger-ui/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}