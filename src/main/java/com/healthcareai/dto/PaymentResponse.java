package com.healthcareai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID tenantId,
        String tenantName,
        BigDecimal amount,
        String currency,
        String description,
        String status,
        String createdBy,
        Instant paidAt,
        Instant createdAt
) {
}
