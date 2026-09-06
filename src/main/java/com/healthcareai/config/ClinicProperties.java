package com.healthcareai.config;

import java.time.LocalTime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clinic-level business configuration used by the AI receptionist
 * (e.g. escalation contact, timezone, working hours, appointment slot duration).
 */
@ConfigurationProperties(prefix = "app.clinic")
public record ClinicProperties(
        String name,
        String timezone,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        int appointmentSlotMinutes,
        int reminderHoursBefore,
        String escalationEmail,
        String escalationPhoneNumber
) {
    public ClinicProperties {
        if (timezone == null || timezone.isBlank()) {
            timezone = "UTC";
        }
        if (workingHoursStart == null) {
            workingHoursStart = LocalTime.of(9, 0);
        }
        if (workingHoursEnd == null) {
            workingHoursEnd = LocalTime.of(17, 0);
        }
        if (appointmentSlotMinutes <= 0) {
            appointmentSlotMinutes = 30;
        }
        if (reminderHoursBefore <= 0) {
            reminderHoursBefore = 24;
        }
    }
}
