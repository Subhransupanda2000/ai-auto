package com.healthcareai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/appointments}.
 */
public record AppointmentRequest(
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        @NotNull Instant start,
        @NotNull Instant end,
        String reason,

        /** Optional consultation fee charged for this visit, entered at
         * booking time; used to compute real (not estimated) revenue. */
        @DecimalMin(value = "0", message = "Consultation fee cannot be negative.")
        BigDecimal consultationFee
) {
}
