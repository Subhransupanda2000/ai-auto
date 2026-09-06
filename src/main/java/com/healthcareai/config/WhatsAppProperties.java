package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the WhatsApp Business (Cloud API) integration.
 */
@ConfigurationProperties(prefix = "app.whatsapp")
public record WhatsAppProperties(
        String baseUrl,
        String phoneNumberId,
        String accessToken,
        boolean enabled
) {
    public WhatsAppProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://graph.facebook.com/v20.0";
        }
    }
}
