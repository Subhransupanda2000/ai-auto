package com.healthcareai.ai.tools;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.config.ClinicProperties;
import com.healthcareai.entity.Channel;
import com.healthcareai.service.ConversationService;
import com.healthcareai.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Lets the AI agent explicitly hand a conversation off to a human
 * receptionist, e.g. when it cannot help, the patient asks for a human, or
 * a request falls outside clinic knowledge/medical-advice boundaries.
 */
@Component
@RequiredArgsConstructor
public class EscalationTool implements ToolProvider {

    private final ConversationService conversationService;
    private final NotificationService notificationService;
    private final ClinicProperties clinicProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToolFunction> getFunctions() {
        return List.of(new SimpleToolFunction(escalateDefinition(), this::escalate));
    }

    private ToolDefinition escalateDefinition() {
        return new ToolDefinition(
                "conversation_escalateToHuman",
                "Escalates the current conversation to a human receptionist. Use this when the patient explicitly "
                        + "asks for a human, when the request is outside clinic knowledge, or when you cannot resolve "
                        + "the request after reasonable attempts.",
                """
                {
                  "type": "object",
                  "properties": {
                    "sessionId": { "type": "string" },
                    "channel": { "type": "string", "enum": ["WEB", "WHATSAPP", "SMS", "EMAIL"] },
                    "patientId": { "type": "string" },
                    "reason": { "type": "string" }
                  },
                  "required": ["sessionId", "reason"]
                }
                """);
    }

    @SneakyThrows
    private String escalate(JsonNode args) {
        String sessionId = ToolArguments.requiredString(args, "sessionId");
        String reason = ToolArguments.requiredString(args, "reason");
        String channelText = ToolArguments.optionalString(args, "channel");
        String patientIdText = ToolArguments.optionalString(args, "patientId");

        Channel channel = channelText != null ? Channel.valueOf(channelText) : Channel.WEB;
        java.util.UUID patientId = patientIdText != null ? java.util.UUID.fromString(patientIdText) : null;

        conversationService.escalate(sessionId, channel, patientId, reason);

        if (clinicProperties.escalationEmail() != null && !clinicProperties.escalationEmail().isBlank()) {
            notificationService.sendEmail(clinicProperties.escalationEmail(),
                    "Conversation escalated: " + sessionId,
                    "A conversation was escalated to a human receptionist.\nSession: " + sessionId + "\nReason: " + reason);
        }
        return objectMapper.writeValueAsString(Map.of("status", "escalated", "sessionId", sessionId));
    }
}
