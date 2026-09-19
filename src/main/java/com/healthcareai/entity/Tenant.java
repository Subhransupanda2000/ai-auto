package com.healthcareai.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A clinic tenant. Every clinical entity (doctors, patients, appointments,
 * conversations, knowledge base documents) and every staff {@link User}
 * belongs to exactly one tenant; tenants are created exclusively by a
 * {@link SuperAdmin} via the tenant onboarding flow.
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Tenant {

    @Id
    @Column(updatable = false, nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    /** URL/subdomain-friendly unique identifier, e.g. {@code "sunrise-clinic"}. */
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Per-clinic feature toggle controlled by a super admin: when {@code
     * false}, {@code AppointmentServiceImpl} skips sending any WhatsApp
     * message (scheduled/rescheduled/cancelled/completed) for this tenant's
     * appointments. Defaults to on for newly onboarded clinics. */
    @Column(name = "whatsapp_notifications_enabled", nullable = false)
    @Builder.Default
    private boolean whatsappNotificationsEnabled = true;

    /** Running count of appointment-lifecycle WhatsApp messages actually
     * sent for this tenant, surfaced to super admins on the Tenants page.
     * Incremented via {@code TenantRepository.incrementWhatsappMessageCount}
     * whenever {@code AppointmentServiceImpl} sends one. */
    @Column(name = "whatsapp_message_count", nullable = false)
    @Builder.Default
    private long whatsappMessageCount = 0;

    /** Per-clinic feature toggle controlled by a super admin: when {@code
     * false}, {@code ChatController} rejects new messages to the AI
     * receptionist (POST /api/chat) for this tenant. Defaults to on for
     * newly onboarded clinics. */
    @Column(name = "ai_chat_enabled", nullable = false)
    @Builder.Default
    private boolean aiChatEnabled = true;

    /** Running count of user messages successfully handled by the AI
     * receptionist for this tenant, surfaced to super admins (and the
     * clinic's own admin) alongside {@code whatsappMessageCount}.
     * Incremented via {@code TenantRepository.incrementAiChatMessageCount}
     * in {@code ChatController}. */
    @Column(name = "ai_chat_message_count", nullable = false)
    @Builder.Default
    private long aiChatMessageCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
