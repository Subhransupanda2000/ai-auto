package com.healthcareai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        String patientName,
        UUID doctorId,
        String doctorName,
        Instant scheduledStart,
        Instant scheduledEnd,
        String status,
        String reason,
        String notes,
        BigDecimal consultationFee
) {
}
