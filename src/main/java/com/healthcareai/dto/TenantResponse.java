package com.healthcareai.dto;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        boolean active,
        long userCount,
        Instant createdAt
) {
}
