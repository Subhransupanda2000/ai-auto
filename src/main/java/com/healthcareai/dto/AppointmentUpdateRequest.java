package com.healthcareai.dto;

import java.time.Instant;

import com.healthcareai.entity.AppointmentStatus;

/**
 * Request body for {@code PUT /api/appointments/{id}}. All fields are
 * optional; provide {@code newStart}/{@code newEnd} together to reschedule,
 * or {@code status} to transition the appointment (e.g. CONFIRMED, COMPLETED).
 */
public record AppointmentUpdateRequest(
        Instant newStart,
        Instant newEnd,
        String reason,
        AppointmentStatus status
) {
}
