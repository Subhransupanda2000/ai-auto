package com.healthcareai.dto;

import java.time.Instant;

public record TenantMessageStatsResponse(
        MessageStatsRange range,
        long whatsappMessageCount,
        long aiChatMessageCount,
        Instant rangeStart,
        Instant rangeEnd
) {
}
