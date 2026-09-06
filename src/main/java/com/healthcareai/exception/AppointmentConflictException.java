package com.healthcareai.exception;

/**
 * Thrown when an appointment cannot be booked/rescheduled because the
 * requested slot overlaps with an existing, non-cancelled appointment for
 * the same doctor.
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
