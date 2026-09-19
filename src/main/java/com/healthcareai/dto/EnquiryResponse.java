package com.healthcareai.dto;

import java.time.Instant;
import java.util.UUID;

public record EnquiryResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        String clinicName,
        String message,
        String status,
        Instant createdAt
) {
}
