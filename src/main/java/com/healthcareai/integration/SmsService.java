package com.healthcareai.integration;

/**
 * Abstraction over the SMS provider (Twilio) used to send patient-facing
 * text messages.
 */
public interface SmsService {

    void sendMessage(String toPhoneNumber, String message);
}
