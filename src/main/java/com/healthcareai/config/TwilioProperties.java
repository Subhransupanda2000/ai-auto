package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Twilio SMS integration.
 */
@ConfigurationProperties(prefix = "app.twilio")
public record TwilioProperties(
        String accountSid,
        String authToken,
        String fromNumber,
        boolean enabled
) {
}
