package com.healthcareai.dto;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/doctors}.
 */
public record DoctorRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String specialty,
        String email,
        String phoneNumber,
        String bio,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        List<String> workingDays
) {
    /** Comma-separated day codes for persistence, e.g. {@code "MON,TUE,WED"}. */
    public String workingDaysCsv() {
        return workingDays == null || workingDays.isEmpty() ? null : String.join(",", workingDays);
    }
}
