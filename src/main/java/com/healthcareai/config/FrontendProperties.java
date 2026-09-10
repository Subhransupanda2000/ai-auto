package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the staff web app is hosted, used to build absolute links (e.g. the
 * password reset link) inside outbound emails.
 */
@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(String baseUrl) {
    public FrontendProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:5173";
        } else if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
    }
}
