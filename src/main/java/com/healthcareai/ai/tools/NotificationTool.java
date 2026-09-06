package com.healthcareai.ai.tools;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Exposes outbound patient notifications (WhatsApp, SMS, Email) to the AI
 * agent, e.g. to send appointment confirmations or reminders on request.
 */
@Component
@RequiredArgsConstructor
public class NotificationTool implements ToolProvider {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToolFunction> getFunctions() {
        return List.of(
                new SimpleToolFunction(sendWhatsAppDefinition(), this::sendWhatsApp),
                new SimpleToolFunction(sendSmsDefinition(), this::sendSms),
                new SimpleToolFunction(sendEmailDefinition(), this::sendEmail));
    }

    private ToolDefinition sendWhatsAppDefinition() {
        return new ToolDefinition(
                "notification_sendWhatsApp",
                "Sends a WhatsApp message to a patient's phone number.",
                """
                {
                  "type": "object",
                  "properties": {
                    "phoneNumber": { "type": "string" },
                    "message": { "type": "string" }
                  },
                  "required": ["phoneNumber", "message"]
                }
                """);
    }

    private ToolDefinition sendSmsDefinition() {
        return new ToolDefinition(
                "notification_sendSms",
                "Sends an SMS text message to a patient's phone number.",
                """
                {
                  "type": "object",
                  "properties": {
                    "phoneNumber": { "type": "string" },
                    "message": { "type": "string" }
                  },
                  "required": ["phoneNumber", "message"]
                }
                """);
    }

    private ToolDefinition sendEmailDefinition() {
        return new ToolDefinition(
                "notification_sendEmail",
                "Sends an email to a patient.",
                """
                {
                  "type": "object",
                  "properties": {
                    "email": { "type": "string" },
                    "subject": { "type": "string" },
                    "body": { "type": "string" }
                  },
                  "required": ["email", "subject", "body"]
                }
                """);
    }

    @SneakyThrows
    private String sendWhatsApp(JsonNode args) {
        String phoneNumber = ToolArguments.requiredString(args, "phoneNumber");
        String message = ToolArguments.requiredString(args, "message");
        notificationService.sendWhatsApp(phoneNumber, message);
        return objectMapper.writeValueAsString(Map.of("status", "sent", "channel", "WHATSAPP"));
    }

    @SneakyThrows
    private String sendSms(JsonNode args) {
        String phoneNumber = ToolArguments.requiredString(args, "phoneNumber");
        String message = ToolArguments.requiredString(args, "message");
        notificationService.sendSms(phoneNumber, message);
        return objectMapper.writeValueAsString(Map.of("status", "sent", "channel", "SMS"));
    }

    @SneakyThrows
    private String sendEmail(JsonNode args) {
        String email = ToolArguments.requiredString(args, "email");
        String subject = ToolArguments.requiredString(args, "subject");
        String body = ToolArguments.requiredString(args, "body");
        notificationService.sendEmail(email, subject, body);
        return objectMapper.writeValueAsString(Map.of("status", "sent", "channel", "EMAIL"));
    }
}
