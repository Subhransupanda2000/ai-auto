package com.healthcareai.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * One row per message actually sent/handled for a tenant on a given
 * {@link MessageChannel} (WhatsApp appointment notification, or AI
 * receptionist chat turn). Append-only and timestamped so
 * {@code TenantMessageLogService.getStats} can break {@code
 * Tenant.whatsappMessageCount}/{@code aiChatMessageCount} (lifetime totals)
 * down into "this month" / "last month" for the super admin and clinic
 * admin UIs, without needing to touch the lifetime counters themselves.
 */
@Entity
@Table(name = "tenant_message_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class TenantMessageLog {

    @Id
    @Column(updatable = false, nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MessageChannel channel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
