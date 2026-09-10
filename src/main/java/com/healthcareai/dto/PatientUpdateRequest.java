package com.healthcareai.dto;

/**
 * Request body for {@code PUT /api/patients/{id}}. All fields are optional;
 * only non-blank fields are applied (see {@code PatientServiceImpl.updatePatient}).
 */
public record PatientUpdateRequest(
        String firstName,
        String lastName,
        String email,
        String gender,
        String notes
) {
}
