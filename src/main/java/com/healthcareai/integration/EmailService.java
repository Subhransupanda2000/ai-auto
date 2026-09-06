package com.healthcareai.integration;

/**
 * Abstraction over the email provider used to send patient-facing and
 * internal (escalation) emails.
 */
public interface EmailService {

    void sendEmail(String toEmail, String subject, String body);
}
