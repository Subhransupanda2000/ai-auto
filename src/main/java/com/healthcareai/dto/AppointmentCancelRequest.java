package com.healthcareai.dto;

/**
 * Optional request body for {@code DELETE /api/appointments/{id}}.
 */
public record AppointmentCancelRequest(String reason) {
}
