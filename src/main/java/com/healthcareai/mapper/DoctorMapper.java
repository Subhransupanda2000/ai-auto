package com.healthcareai.mapper;

import java.util.Arrays;
import java.util.List;

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
                doctor.isActive(),
                doctor.getWorkingHoursStart(),
                doctor.getWorkingHoursEnd(),
                toWorkingDaysList(doctor.getWorkingDays()));
    }

    private List<String> toWorkingDaysList(String workingDays) {
        if (workingDays == null || workingDays.isBlank()) {
            return List.of();
        }
        return Arrays.stream(workingDays.split(",")).map(String::trim).toList();
    }
}
