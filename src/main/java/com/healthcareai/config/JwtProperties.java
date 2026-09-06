package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for JWT issuance/validation.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays,
        String issuer
) {
    public JwtProperties {
        if (accessTokenTtlMinutes <= 0) {
            accessTokenTtlMinutes = 60;
        }
        if (refreshTokenTtlDays <= 0) {
            refreshTokenTtlDays = 7;
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "healthcareai";
        }
    }
}
