package com.healthcareai.mapper;

import org.springframework.stereotype.Component;

import com.healthcareai.dto.DoctorResponse;
import com.healthcareai.entity.Doctor;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialty(),
                doctor.getEmail(),
                doctor.getPhoneNumber(),
                doctor.getBio(),
                doctor.isActive());
    }
}
