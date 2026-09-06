package com.healthcareai.integration.whatsapp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcareai.config.WhatsAppProperties;
import com.healthcareai.exception.LlmServiceException;
import com.healthcareai.integration.WhatsAppService;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link WhatsAppService} implementation backed by the WhatsApp Cloud API.
 */
@Component
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final RestClient whatsAppRestClient;
    private final WhatsAppProperties properties;

    public WhatsAppServiceImpl(@Qualifier("whatsAppRestClient") RestClient whatsAppRestClient,
                                WhatsAppProperties properties) {
        this.whatsAppRestClient = whatsAppRestClient;
        this.properties = properties;
    }

    @Override
    public void sendMessage(String toPhoneNumber, String message) {
        if (!properties.enabled()) {
            log.info("WhatsApp integration disabled; skipping message to {}", toPhoneNumber);
            return;
        }
        try {
            whatsAppRestClient.post()
                    .uri("/{phoneNumberId}/messages", properties.phoneNumberId())
                    .body(new WhatsAppMessageRequest("whatsapp", toPhoneNumber, "text",
                            new WhatsAppMessageRequest.TextBody(message)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to send WhatsApp message to {}", toPhoneNumber, e);
            throw new LlmServiceException("Failed to send WhatsApp message.", e);
        }
    }

    private record WhatsAppMessageRequest(
            @JsonProperty("messaging_product") String messagingProduct,
            String to,
            String type,
            TextBody text
    ) {
        private record TextBody(String body) {
        }
    }
}
