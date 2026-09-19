package com.healthcareai.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcareai.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    /** Atomic counter bump so concurrent appointment-notification sends
     * for the same tenant never lose an increment to a read-modify-write
     * race (see AppointmentServiceImpl). */
    @Modifying
    @Query("UPDATE Tenant t SET t.whatsappMessageCount = t.whatsappMessageCount + 1 WHERE t.id = :tenantId")
    void incrementWhatsappMessageCount(@Param("tenantId") UUID tenantId);

    /** Same atomic-increment pattern as {@link #incrementWhatsappMessageCount},
     * bumped once per user message the AI receptionist successfully
     * handles for the tenant (see ChatController). */
    @Modifying
    @Query("UPDATE Tenant t SET t.aiChatMessageCount = t.aiChatMessageCount + 1 WHERE t.id = :tenantId")
    void incrementAiChatMessageCount(@Param("tenantId") UUID tenantId);
}
