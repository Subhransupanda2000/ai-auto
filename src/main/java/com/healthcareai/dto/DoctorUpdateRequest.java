package com.healthcareai.dto;

import java.time.LocalTime;
import java.util.List;

/**
 * Request body for {@code PUT /api/doctors/{id}}. All fields are optional;
 * only non-blank/non-null fields are applied.
 */
public record DoctorUpdateRequest(
        String specialty,
        String email,
        String phoneNumber,
        String bio,
        Boolean active,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        List<String> workingDays
) {
    public String workingDaysCsv() {
        return workingDays == null || workingDays.isEmpty() ? null : String.join(",", workingDays);
    }
}
