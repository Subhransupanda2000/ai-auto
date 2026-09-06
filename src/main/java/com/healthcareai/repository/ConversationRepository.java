package com.healthcareai.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * Returns the most recent messages for a session (used to build bounded
     * conversation context for the LLM), most-recent-first.
     */
    List<Conversation> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    List<Conversation> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
