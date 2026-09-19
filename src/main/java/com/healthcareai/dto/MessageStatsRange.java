package com.healthcareai.dto;

/**
 * Message-count window options for the super-admin Tenants list and the
 * clinic admin's own Settings page. {@code ALL_TIME} reports {@code
 * Tenant.whatsappMessageCount}/{@code aiChatMessageCount} directly (no log
 * table scan); {@code THIS_MONTH}/{@code LAST_MONTH} are resolved against
 * the clinic timezone from {@code TenantMessageLog} (see
 * {@code TenantMessageLogService}).
 */
public enum MessageStatsRange {
    ALL_TIME,
    THIS_MONTH,
    LAST_MONTH
}
