package com.healthcareai.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        String firstName,
        String lastName,
        String specialty,
        String email,
        String phoneNumber,
        String bio,
        boolean active,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        List<String> workingDays
) {
}
