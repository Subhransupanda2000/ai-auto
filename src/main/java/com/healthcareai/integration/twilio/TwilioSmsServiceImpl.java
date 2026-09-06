package com.healthcareai.integration.twilio;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.healthcareai.config.TwilioProperties;
import com.healthcareai.exception.LlmServiceException;
import com.healthcareai.integration.SmsService;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link SmsService} implementation backed by the Twilio Messages REST API.
 */
@Component
@Slf4j
public class TwilioSmsServiceImpl implements SmsService {

    private final RestClient twilioRestClient;
    private final TwilioProperties properties;

    public TwilioSmsServiceImpl(@Qualifier("twilioRestClient") RestClient twilioRestClient,
                                 TwilioProperties properties) {
        this.twilioRestClient = twilioRestClient;
        this.properties = properties;
    }

    @Override
    public void sendMessage(String toPhoneNumber, String message) {
        if (!properties.enabled()) {
            log.info("Twilio SMS integration disabled; skipping message to {}", toPhoneNumber);
            return;
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", toPhoneNumber);
            form.add("From", properties.fromNumber());
            form.add("Body", message);

            twilioRestClient.post()
                    .uri("/Accounts/{accountSid}/Messages.json", properties.accountSid())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to send SMS to {}", toPhoneNumber, e);
            throw new LlmServiceException("Failed to send SMS.", e);
        }
    }
}
