package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Controls the one-time demo data seeder ({@code com.healthcareai.seed.DemoDataSeeder}).
 * See {@code app.seed.*} in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
        boolean enabled,
        int doctorCount,
        int patientCount,
        int appointmentCount,
        int knowledgeBaseArticleCount,
        int faqCount,
        String defaultTenantName,
        String defaultTenantSlug,
        String defaultAdminEmail,
        String defaultAdminPassword,
        String defaultAdminFullName
) {
    public SeedProperties {
        if (doctorCount <= 0) {
            doctorCount = 15;
        }
        if (patientCount <= 0) {
            patientCount = 200;
        }
        if (appointmentCount <= 0) {
            appointmentCount = 500;
        }
        if (knowledgeBaseArticleCount <= 0) {
            knowledgeBaseArticleCount = 100;
        }
        if (faqCount <= 0) {
            faqCount = 50;
        }
        if (defaultTenantName == null || defaultTenantName.isBlank()) {
            defaultTenantName = "Demo Clinic";
        }
        if (defaultTenantSlug == null || defaultTenantSlug.isBlank()) {
            defaultTenantSlug = "demo-clinic";
        }
        if (defaultAdminFullName == null || defaultAdminFullName.isBlank()) {
            defaultAdminFullName = "Demo Admin";
        }
    }
}
