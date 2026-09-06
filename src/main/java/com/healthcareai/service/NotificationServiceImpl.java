package com.healthcareai.service;

import org.springframework.stereotype.Service;

import com.healthcareai.integration.EmailService;
import com.healthcareai.integration.SmsService;
import com.healthcareai.integration.WhatsAppService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final WhatsAppService whatsAppService;
    private final SmsService smsService;
    private final EmailService emailService;

    @Override
    public void sendWhatsApp(String toPhoneNumber, String message) {
        whatsAppService.sendMessage(toPhoneNumber, message);
    }

    @Override
    public void sendSms(String toPhoneNumber, String message) {
        smsService.sendMessage(toPhoneNumber, message);
    }

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        emailService.sendEmail(toEmail, subject, body);
    }
}
