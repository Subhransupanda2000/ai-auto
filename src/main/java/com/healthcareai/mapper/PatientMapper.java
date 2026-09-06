package com.healthcareai.mapper;

import org.springframework.stereotype.Component;

import com.healthcareai.dto.PatientResponse;
import com.healthcareai.entity.Patient;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhoneNumber(),
                patient.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender());
    }
}
