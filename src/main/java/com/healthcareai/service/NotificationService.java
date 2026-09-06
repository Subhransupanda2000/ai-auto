package com.healthcareai.service;

/**
 * Facade over the outbound communication channels (WhatsApp, SMS, Email)
 * used to send appointment confirmations, reminders, and other patient
 * notifications. Implemented in terms of the channel-specific integration
 * interfaces (see {@code com.healthcareai.integration}).
 */
public interface NotificationService {

    void sendWhatsApp(String toPhoneNumber, String message);

    void sendSms(String toPhoneNumber, String message);

    void sendEmail(String toEmail, String subject, String body);
}
