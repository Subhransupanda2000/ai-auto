package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials for the one-time super-admin bootstrap
 * ({@code com.healthcareai.seed.SuperAdminBootstrap}). Both {@code email}
 * and {@code password} must be supplied via environment variables
 * ({@code SUPER_ADMIN_EMAIL} / {@code SUPER_ADMIN_PASSWORD}) - there is
 * deliberately no hardcoded default, since this account is the root of
 * trust for onboarding every tenant.
 */
@ConfigurationProperties(prefix = "app.super-admin")
public record SuperAdminBootstrapProperties(String email, String password, String fullName) {
    public SuperAdminBootstrapProperties {
        if (fullName == null || fullName.isBlank()) {
            fullName = "Platform Super Admin";
        }
    }
}
