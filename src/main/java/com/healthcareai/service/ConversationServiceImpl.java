package com.healthcareai.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.entity.Channel;
import com.healthcareai.entity.Conversation;
import com.healthcareai.entity.MessageRole;
import com.healthcareai.repository.ConversationRepository;
import com.healthcareai.repository.PatientRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public Conversation logMessage(String sessionId, Channel channel, MessageRole role, String message,
                                    Map<String, Object> metadata, UUID patientId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Conversation.ConversationBuilder builder = Conversation.builder()
                .tenantId(tenantId)
                .sessionId(sessionId)
                .channel(channel)
                .role(role)
                .message(message)
                .metadata(metadata);

        if (patientId != null) {
            patientRepository.findByIdAndTenantId(patientId, tenantId).ifPresent(builder::patient);
        }
        return conversationRepository.save(builder.build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getHistory(String sessionId) {
        return conversationRepository.findByTenantIdAndSessionIdOrderByCreatedAtAsc(TenantContext.getCurrentTenantId(), sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getRecentContext(String sessionId, int maxMessages) {
        List<Conversation> mostRecentFirst = conversationRepository.findByTenantIdAndSessionIdOrderByCreatedAtDesc(
                TenantContext.getCurrentTenantId(), sessionId, PageRequest.of(0, Math.max(maxMessages, 1)));
        Collections.reverse(mostRecentFirst);
        return mostRecentFirst;
    }

    @Override
    @Transactional
    public Conversation escalate(String sessionId, Channel channel, UUID patientId, String reason) {
        Conversation escalation = logMessage(sessionId, channel, MessageRole.SYSTEM,
                "Conversation escalated to a human receptionist. Reason: " + reason,
                Map.of("escalationReason", reason), patientId);
        escalation.setEscalated(true);
        return conversationRepository.save(escalation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> findByPatient(UUID patientId) {
        return conversationRepository.findByTenantIdAndPatientIdOrderByCreatedAtDesc(TenantContext.getCurrentTenantId(), patientId);
    }
}
