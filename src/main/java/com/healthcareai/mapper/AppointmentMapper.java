package com.healthcareai.mapper;

import org.springframework.stereotype.Component;

import com.healthcareai.dto.AppointmentResponse;
import com.healthcareai.entity.Appointment;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getFullName(),
                appointment.getScheduledStart(),
                appointment.getScheduledEnd(),
                appointment.getStatus().name(),
                appointment.getReason(),
                appointment.getNotes(),
                appointment.getConsultationFee());
    }
}
