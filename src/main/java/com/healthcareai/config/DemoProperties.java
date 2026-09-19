package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the public "Request a Demo" read-only preview (see
 * {@code EnquiryService}/{@code DemoJwtService}): which already-seeded
 * tenant demo sessions are scoped to, and how long a demo session lasts.
 */
@ConfigurationProperties(prefix = "app.demo")
public record DemoProperties(String tenantSlug, long accessTokenTtlMinutes) {

    public DemoProperties {
        if (tenantSlug == null || tenantSlug.isBlank()) {
            tenantSlug = "demo-clinic";
        }
        if (accessTokenTtlMinutes <= 0) {
            accessTokenTtlMinutes = 60;
        }
    }
}
