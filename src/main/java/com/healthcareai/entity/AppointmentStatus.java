package com.healthcareai.entity;

/**
 * Lifecycle states of an {@link Appointment}.
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    RESCHEDULED,
    NO_SHOW
}
