package com.healthcareai.ai.tools;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.entity.Appointment;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.service.AppointmentService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Exposes appointment record CRUD to the AI agent. Booking/rescheduling
 * that must also stay in sync with the external calendar is handled by
 * {@link CalendarTool}; this tool covers direct lookups and administrative
 * updates (status changes, notes) against our own records.
 */
@Component
@RequiredArgsConstructor
public class AppointmentTool implements ToolProvider {

    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToolFunction> getFunctions() {
        return List.of(
                new SimpleToolFunction(createAppointmentDefinition(), this::createAppointment),
                new SimpleToolFunction(findAppointmentDefinition(), this::findAppointment),
                new SimpleToolFunction(updateAppointmentDefinition(), this::updateAppointment),
                new SimpleToolFunction(cancelAppointmentDefinition(), this::cancelAppointment));
    }

    private ToolDefinition createAppointmentDefinition() {
        return new ToolDefinition(
                "appointment_createAppointment",
                "Creates an appointment record for a patient with a doctor at a specific time. The slot's "
                        + "availability should already have been verified with calendar_checkAvailability.",
                """
                {
                  "type": "object",
                  "properties": {
                    "patientId": { "type": "string" },
                    "doctorId": { "type": "string" },
                    "start": { "type": "string", "description": "ISO-8601 start timestamp, e.g. 2025-01-31T14:30:00Z." },
                    "end": { "type": "string", "description": "ISO-8601 end timestamp." },
                    "reason": { "type": "string" }
                  },
                  "required": ["patientId", "doctorId", "start", "end"]
                }
                """);
    }

    private ToolDefinition findAppointmentDefinition() {
        return new ToolDefinition(
                "appointment_findAppointment",
                "Finds appointments by appointment id, patient id, or doctor id.",
                """
                {
                  "type": "object",
                  "properties": {
                    "appointmentId": { "type": "string" },
                    "patientId": { "type": "string" },
                    "doctorId": { "type": "string" }
                  }
                }
                """);
    }

    private ToolDefinition updateAppointmentDefinition() {
        return new ToolDefinition(
                "appointment_updateAppointment",
                "Updates an appointment's reason/notes, or marks it CONFIRMED or COMPLETED. To change the "
                        + "date/time, use calendar_rescheduleAppointment instead.",
                """
                {
                  "type": "object",
                  "properties": {
                    "appointmentId": { "type": "string" },
                    "reason": { "type": "string" },
                    "notes": { "type": "string" },
                    "status": { "type": "string", "enum": ["CONFIRMED", "COMPLETED"] }
                  },
                  "required": ["appointmentId"]
                }
                """);
    }

    private ToolDefinition cancelAppointmentDefinition() {
        return new ToolDefinition(
                "appointment_cancelAppointment",
                "Cancels an existing appointment.",
                """
                {
                  "type": "object",
                  "properties": {
                    "appointmentId": { "type": "string" },
                    "reason": { "type": "string" }
                  },
                  "required": ["appointmentId"]
                }
                """);
    }

    @SneakyThrows
    private String createAppointment(JsonNode args) {
        UUID patientId = ToolArguments.requiredUuid(args, "patientId");
        UUID doctorId = ToolArguments.requiredUuid(args, "doctorId");
        var start = ToolArguments.requiredInstant(args, "start");
        var end = ToolArguments.requiredInstant(args, "end");
        String reason = ToolArguments.optionalString(args, "reason");

        Appointment appointment = appointmentService.bookAppointment(patientId, doctorId, start, end, reason, null);
        return objectMapper.writeValueAsString(AppointmentView.from(appointment));
    }

    @SneakyThrows
    private String findAppointment(JsonNode args) {
        String appointmentId = ToolArguments.optionalString(args, "appointmentId");
        String patientId = ToolArguments.optionalString(args, "patientId");
        String doctorId = ToolArguments.optionalString(args, "doctorId");

        if (appointmentId != null) {
            Optional<Appointment> appointment = appointmentService.findById(UUID.fromString(appointmentId));
            return objectMapper.writeValueAsString(appointment.map(AppointmentView::from).orElse(null));
        }
        if (patientId != null) {
            List<AppointmentView> views = appointmentService.findByPatient(UUID.fromString(patientId)).stream()
                    .map(AppointmentView::from).toList();
            return objectMapper.writeValueAsString(views);
        }
        if (doctorId != null) {
            List<AppointmentView> views = appointmentService.findByDoctor(UUID.fromString(doctorId)).stream()
                    .map(AppointmentView::from).toList();
            return objectMapper.writeValueAsString(views);
        }
        throw new BusinessRuleViolationException("Provide at least one of appointmentId, patientId, or doctorId.");
    }

    @SneakyThrows
    private String updateAppointment(JsonNode args) {
        UUID appointmentId = ToolArguments.requiredUuid(args, "appointmentId");
        String status = ToolArguments.optionalString(args, "status");

        Appointment appointment;
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            appointment = appointmentService.confirmAppointment(appointmentId);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            appointment = appointmentService.completeAppointment(appointmentId);
        } else {
            appointment = appointmentService.findById(appointmentId)
                    .orElseThrow(() -> new BusinessRuleViolationException("Appointment not found: " + appointmentId));
        }
        return objectMapper.writeValueAsString(AppointmentView.from(appointment));
    }

    @SneakyThrows
    private String cancelAppointment(JsonNode args) {
        UUID appointmentId = ToolArguments.requiredUuid(args, "appointmentId");
        String reason = ToolArguments.optionalString(args, "reason");
        Appointment appointment = appointmentService.cancelAppointment(appointmentId, reason);
        return objectMapper.writeValueAsString(AppointmentView.from(appointment));
    }

    private record AppointmentView(String id, String patientId, String doctorId, String start, String end,
                                    String status, String reason) {
        static AppointmentView from(Appointment appointment) {
            return new AppointmentView(
                    appointment.getId().toString(),
                    appointment.getPatient().getId().toString(),
                    appointment.getDoctor().getId().toString(),
                    appointment.getScheduledStart().toString(),
                    appointment.getScheduledEnd().toString(),
                    appointment.getStatus().name(),
                    appointment.getReason());
        }
    }
}
