package com.healthcareai.integration.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.healthcareai.exception.LlmServiceException;
import com.healthcareai.integration.EmailService;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link EmailService} implementation backed by Spring's {@link JavaMailSender}
 * (SMTP).
 */
@Component
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender, @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        if (fromAddress == null || fromAddress.isBlank()) {
            // Dev-only fallback: with no SMTP credentials configured
            // (spring.mail.username / MAIL_USERNAME), there's no way to
            // actually deliver this, so print it in full instead of
            // silently dropping it - this is the only way to get e.g. a
            // password reset link without setting up a real mail provider.
            log.info("Email integration not configured (spring.mail.username is empty); "
                            + "printing email instead of sending it.\nTo: {}\nSubject: {}\n\n{}\n",
                    toEmail, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email to {}", toEmail, e);
            throw new LlmServiceException("Failed to send email.", e);
        }
    }
}
