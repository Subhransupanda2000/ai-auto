package com.healthcareai.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/patients}.
 */
public record PatientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        String email,
        LocalDate dateOfBirth,
        String gender
) {
}
