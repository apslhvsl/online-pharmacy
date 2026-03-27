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