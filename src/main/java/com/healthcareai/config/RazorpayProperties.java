package com.healthcareai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Razorpay payments integration, used behind
 * {@code com.healthcareai.integration.razorpay.RazorpayClient}. {@code
 * keyId}/{@code keySecret} come from the Razorpay Dashboard's API Keys
 * screen (test or live mode); {@code webhookSecret} is set when
 * configuring the webhook endpoint there. None of these ever identify or
 * transmit a bank account directly - Razorpay settles collected payments
 * to whichever bank account was linked during their own KYC/onboarding.
 */
@ConfigurationProperties(prefix = "app.razorpay")
public record RazorpayProperties(
        String keyId,
        String keySecret,
        String webhookSecret,
        String baseUrl,
        int timeoutSeconds
) {

    public RazorpayProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.razorpay.com/v1";
        }
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 20;
        }
    }
}
