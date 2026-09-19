package com.healthcareai.dto;

/**
 * Read-only, non-sensitive view of the caller's own tenant, returned by
 * {@code GET /api/tenant/me} (ADMIN only). Unlike {@link TenantResponse},
 * this is reachable by a clinic's own staff (not just a super admin) and
 * intentionally exposes only the feature-toggle status and usage counters
 * a clinic admin needs to see - never other tenants' data.
 */
public record TenantSelfResponse(
        String name,
        String slug,
        boolean active,
        boolean whatsappNotificationsEnabled,
        long whatsappMessageCount,
        boolean aiChatEnabled,
        long aiChatMessageCount
) {
}
