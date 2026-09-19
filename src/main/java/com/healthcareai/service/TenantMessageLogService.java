package com.healthcareai.service;

import java.util.UUID;

import com.healthcareai.dto.MessageStatsRange;
import com.healthcareai.dto.TenantMessageStatsResponse;

/**
 * Records individual WhatsApp/AI-chat message sends for a tenant (also
 * bumping the {@code Tenant.whatsappMessageCount}/{@code
 * aiChatMessageCount} lifetime counters) and answers "how many messages
 * did this clinic send this month / last month" for the super admin and
 * clinic admin UIs.
 */
public interface TenantMessageLogService {

    void recordWhatsappMessage(UUID tenantId);

    void recordAiChatMessage(UUID tenantId);

    TenantMessageStatsResponse getStats(UUID tenantId, MessageStatsRange range);
}
