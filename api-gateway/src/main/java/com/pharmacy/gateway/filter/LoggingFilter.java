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
        // can include it in their own log lines — full trace with one ID
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
            // Warn on 4xx/5xx so security events are easy to grep in prod logs
            if (status >= 400) {
                log.warn("RESPONSE | id={} status={} duration={}ms", requestId, status, duration);
            } else {
                log.info("RESPONSE | id={} status={} duration={}ms", requestId, status, duration);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -2; // Must run before JwtAuthFilter so X-Request-Id is available
    }
}
