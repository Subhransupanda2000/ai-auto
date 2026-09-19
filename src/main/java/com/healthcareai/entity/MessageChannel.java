package com.healthcareai.entity;

/**
 * Which outbound/AI channel a {@link TenantMessageLog} row records - used
 * to break down a tenant's message volume (see
 * {@code TenantMessageLogService.getStats}) independently for the WhatsApp
 * appointment notifications and the AI receptionist chat.
 */
public enum MessageChannel {
    WHATSAPP,
    AI_CHAT
}
