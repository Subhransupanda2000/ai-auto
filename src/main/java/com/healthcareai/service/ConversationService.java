package com.healthcareai.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.healthcareai.entity.Channel;
import com.healthcareai.entity.Conversation;
import com.healthcareai.entity.MessageRole;

public interface ConversationService {

    Conversation logMessage(String sessionId, Channel channel, MessageRole role, String message,
                             Map<String, Object> metadata, UUID patientId);

    /** Full conversation history for a session, oldest first. */
    List<Conversation> getHistory(String sessionId);

    /**
     * The most recent {@code maxMessages} messages for a session, returned
     * oldest first, suitable for building bounded LLM context.
     */
    List<Conversation> getRecentContext(String sessionId, int maxMessages);

    Conversation escalate(String sessionId, Channel channel, UUID patientId, String reason);

    List<Conversation> findByPatient(UUID patientId);
}
