package com.healthcareai.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByTenantIdAndSessionIdOrderByCreatedAtAsc(UUID tenantId, String sessionId);

    /**
     * Returns the most recent messages for a session (used to build bounded
     * conversation context for the LLM), most-recent-first.
     */
    List<Conversation> findByTenantIdAndSessionIdOrderByCreatedAtDesc(UUID tenantId, String sessionId, Pageable pageable);

    List<Conversation> findByTenantIdAndPatientIdOrderByCreatedAtDesc(UUID tenantId, UUID patientId);
}
