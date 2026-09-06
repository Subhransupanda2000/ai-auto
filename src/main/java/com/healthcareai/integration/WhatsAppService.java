package com.healthcareai.integration;

/**
 * Abstraction over the WhatsApp Business (Cloud API) integration used to
 * send patient-facing messages.
 */
public interface WhatsAppService {

    void sendMessage(String toPhoneNumber, String message);
}
