package com.healthcareai.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.entity.Tenant;

public interface TenantService {

    /** Creates a new tenant and its first (ADMIN) staff user in one
     * transaction. Only reachable by a super admin. */
    Tenant createTenantWithAdmin(String tenantName, String slug, String adminFullName,
                                  String adminEmail, String adminPassword);

    List<Tenant> findAll();

    Optional<Tenant> findById(UUID tenantId);

    Tenant setActive(UUID tenantId, boolean active);

    /** Super-admin-only toggle for whether {@code AppointmentServiceImpl}
     * sends appointment-lifecycle WhatsApp messages (scheduled/rescheduled/
     * cancelled/completed) to this tenant's patients. */
    Tenant setWhatsappNotificationsEnabled(UUID tenantId, boolean enabled);

    /** Super-admin-only toggle for whether {@code ChatController} accepts
     * new messages to the AI receptionist for this tenant. */
    Tenant setAiChatEnabled(UUID tenantId, boolean enabled);
}
