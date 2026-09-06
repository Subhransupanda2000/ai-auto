package com.healthcareai.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable CORS allow-list. Defaults to no cross-origin access; set
 * {@code app.cors.allowed-origins} explicitly for known API consumers
 * (e.g. an n8n instance or a partner integration).
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of();
        }
    }
}
