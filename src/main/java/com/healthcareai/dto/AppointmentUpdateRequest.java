package com.healthcareai.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.healthcareai.entity.AppointmentStatus;

/**
 * Request body for {@code PUT /api/appointments/{id}}. All fields are
 * optional; provide {@code newStart}/{@code newEnd} together to reschedule,
 * {@code status} to transition the appointment (e.g. CONFIRMED, COMPLETED),
 * and/or {@code consultationFee} to set/correct the charged amount.
 */
public record AppointmentUpdateRequest(
        Instant newStart,
        Instant newEnd,
        String reason,
        AppointmentStatus status,
        BigDecimal consultationFee
) {
}
