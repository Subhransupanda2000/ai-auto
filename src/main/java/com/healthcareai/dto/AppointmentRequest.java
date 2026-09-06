package com.healthcareai.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/appointments}.
 */
public record AppointmentRequest(
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        @NotNull Instant start,
        @NotNull Instant end,
        String reason
) {
}
