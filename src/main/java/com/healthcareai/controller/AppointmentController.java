package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.AppointmentCancelRequest;
import com.healthcareai.dto.AppointmentRequest;
import com.healthcareai.dto.AppointmentResponse;
import com.healthcareai.dto.AppointmentUpdateRequest;
import com.healthcareai.dto.RevenueRange;
import com.healthcareai.dto.RevenueResponse;
import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.AppointmentStatus;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.mapper.AppointmentMapper;
import com.healthcareai.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Appointment management: {@code GET/POST/PUT/DELETE /api/appointments}.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @GetMapping
    @Operation(summary = "List appointments, optionally filtered by patient or doctor.")
    public ResponseEntity<List<AppointmentResponse>> list(@RequestParam(required = false) UUID patientId,
                                                            @RequestParam(required = false) UUID doctorId) {
        List<Appointment> appointments;
        if (patientId != null) {
            appointments = appointmentService.findByPatient(patientId);
        } else if (doctorId != null) {
            appointments = appointmentService.findByDoctor(doctorId);
        } else {
            appointments = appointmentService.findAll();
        }
        return ResponseEntity.ok(appointments.stream().map(appointmentMapper::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "Book a new appointment.")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentService.bookAppointment(
                request.patientId(), request.doctorId(), request.start(), request.end(), request.reason(),
                request.consultationFee());
        return ResponseEntity.ok(appointmentMapper.toResponse(appointment));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Real revenue (sum of consultation fees on completed appointments) for a rolling date range.")
    public ResponseEntity<RevenueResponse> revenue(
            @RequestParam(required = false, defaultValue = "TODAY") RevenueRange range) {
        return ResponseEntity.ok(appointmentService.getRevenueSummary(range));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reschedule an appointment, transition its status, and/or update its consultation fee.")
    public ResponseEntity<AppointmentResponse> update(@PathVariable UUID id,
                                                        @RequestBody AppointmentUpdateRequest request) {
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", id));

        if (request.newStart() != null && request.newEnd() != null) {
            appointment = appointmentService.rescheduleAppointment(id, request.newStart(), request.newEnd());
        }
        if (request.status() == AppointmentStatus.CONFIRMED) {
            appointment = appointmentService.confirmAppointment(id);
        } else if (request.status() == AppointmentStatus.COMPLETED) {
            appointment = appointmentService.completeAppointment(id);
        }
        if (request.consultationFee() != null) {
            appointment = appointmentService.updateConsultationFee(id, request.consultationFee());
        }
        return ResponseEntity.ok(appointmentMapper.toResponse(appointment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an appointment.")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable UUID id,
                                                        @RequestBody(required = false) AppointmentCancelRequest request) {
        String reason = request != null ? request.reason() : null;
        Appointment cancelled = appointmentService.cancelAppointment(id, reason);
        return ResponseEntity.ok(appointmentMapper.toResponse(cancelled));
    }
}
