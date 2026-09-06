package com.healthcareai.config;

import java.time.Duration;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Provides named {@link RestClient} beans for each external integration,
 * pre-configured with the appropriate base URL and timeouts. Integration
 * services (Module 10) depend on these beans rather than constructing
 * their own HTTP clients.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient geminiRestClient(RestClient.Builder builder, GeminiProperties properties) {
        RestClient.Builder configured = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties.timeoutSeconds()));
        // Gemini authenticates via the x-goog-api-key header rather than
        // an Authorization bearer token.
        if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
            configured.defaultHeader("x-goog-api-key", properties.apiKey());
        }
        return configured.build();
    }

    @Bean
    public RestClient whatsAppRestClient(RestClient.Builder builder, WhatsAppProperties properties) {
        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(20))
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .build();
    }

    @Bean
    public RestClient twilioRestClient(RestClient.Builder builder, TwilioProperties properties) {
        String credentials = properties.accountSid() + ":" + properties.authToken();
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return builder
                .baseUrl("https://api.twilio.com/2010-04-01")
                .requestFactory(requestFactory(20))
                .defaultHeader("Authorization", "Basic " + basicAuth)
                .build();
    }

    @Bean
    public RestClient n8nRestClient(RestClient.Builder builder, N8nProperties properties) {
        return builder
                .baseUrl(properties.webhookBaseUrl() == null ? "" : properties.webhookBaseUrl())
                .requestFactory(requestFactory(20))
                .build();
    }

    private ClientHttpRequestFactory requestFactory(int timeoutSeconds) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 10)))
                .withReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return ClientHttpRequestFactories.get(settings);
    }
}
