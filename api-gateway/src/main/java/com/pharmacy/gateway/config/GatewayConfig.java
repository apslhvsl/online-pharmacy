package com.pharmacy.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
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
}