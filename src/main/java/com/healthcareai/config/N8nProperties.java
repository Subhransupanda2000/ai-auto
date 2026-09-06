package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for outbound/inbound n8n workflow automation webhooks.
 */
@ConfigurationProperties(prefix = "app.n8n")
public record N8nProperties(
        String webhookBaseUrl,
        String inboundWebhookSecret,
        boolean enabled
) {
}
