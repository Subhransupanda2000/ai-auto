package com.healthcareai.dto;

import java.util.UUID;

public record DoctorResponse(
        UUID id,
        String firstName,
        String lastName,
        String specialty,
        String email,
        String phoneNumber,
        String bio,
        boolean active
) {
}
