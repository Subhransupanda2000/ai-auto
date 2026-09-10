package com.healthcareai.ai.tools;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.ai.ToolDefinition;
import com.healthcareai.dto.AvailableSlot;
import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.Doctor;
import com.healthcareai.entity.Patient;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.integration.CalendarIntegrationService;
import com.healthcareai.service.AppointmentService;
import com.healthcareai.service.DoctorService;
import com.healthcareai.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Exposes availability checking and calendar-synced booking actions to the
 * AI agent. Every booking/reschedule/cancel here also attempts to keep the
 * doctor's Google Calendar in sync via {@link CalendarIntegrationService};
 * calendar sync failures are logged but never block the underlying
 * appointment record change (see the Calendar failure fallback policy).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarTool implements ToolProvider {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final CalendarIntegrationService calendarIntegrationService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToolFunction> getFunctions() {
        return List.of(
                new SimpleToolFunction(checkAvailabilityDefinition(), this::checkAvailability),
                new SimpleToolFunction(bookAppointmentDefinition(), this::bookAppointment),
                new SimpleToolFunction(cancelAppointmentDefinition(), this::cancelAppointment),
                new SimpleToolFunction(rescheduleAppointmentDefinition(), this::rescheduleAppointment));
    }

    private ToolDefinition checkAvailabilityDefinition() {
        return new ToolDefinition(
                "calendar_checkAvailability",
                "Returns the free appointment slots for a doctor on a given date. Always call this before booking "
                        + "or rescheduling an appointment.",
                """
                {
                  "type": "object",
                  "properties": {
                    "doctorId": { "type": "string" },
                    "date": { "type": "string", "description": "ISO-8601 date, e.g. 2025-01-31." }
                  },
                  "required": ["doctorId", "date"]
                }
                """);
    }

    private ToolDefinition bookAppointmentDefinition() {
        return new ToolDefinition(
                "calendar_bookAppointment",
                "Books an appointment for a patient with a doctor at a specific, already-verified-available time, "
                        + "and syncs it to the doctor's calendar.",
                """
                {
                  "type": "object",
                  "properties": {
                    "patientId": { "type": "string" },
                    "doctorId": { "type": "string" },
                    "start": { "type": "string", "description": "ISO-8601 start timestamp." },
                    "end": { "type": "string", "description": "ISO-8601 end timestamp." },
                    "reason": { "type": "string" }
                  },
                  "required": ["patientId", "doctorId", "start", "end"]
                }
                """);
    }

    private ToolDefinition cancelAppointmentDefinition() {
        return new ToolDefinition(
                "calendar_cancelAppointment",
                "Cancels a booked appointment and removes the corresponding calendar event.",
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

    private ToolDefinition rescheduleAppointmentDefinition() {
        return new ToolDefinition(
                "calendar_rescheduleAppointment",
                "Moves an existing appointment to a new, already-verified-available time and updates the calendar event.",
                """
                {
                  "type": "object",
                  "properties": {
                    "appointmentId": { "type": "string" },
                    "newStart": { "type": "string", "description": "ISO-8601 start timestamp." },
                    "newEnd": { "type": "string", "description": "ISO-8601 end timestamp." }
                  },
                  "required": ["appointmentId", "newStart", "newEnd"]
                }
                """);
    }

    @SneakyThrows
    private String checkAvailability(JsonNode args) {
        UUID doctorId = ToolArguments.requiredUuid(args, "doctorId");
        LocalDate date = ToolArguments.requiredDate(args, "date");
        List<AvailableSlot> slots = appointmentService.checkAvailability(doctorId, date);
        return objectMapper.writeValueAsString(slots);
    }

    @SneakyThrows
    private String bookAppointment(JsonNode args) {
        UUID patientId = ToolArguments.requiredUuid(args, "patientId");
        UUID doctorId = ToolArguments.requiredUuid(args, "doctorId");
        Instant start = ToolArguments.requiredInstant(args, "start");
        Instant end = ToolArguments.requiredInstant(args, "end");
        String reason = ToolArguments.optionalString(args, "reason");

        // The AI receptionist never sets a consultation fee; that's entered
        // by staff (see AppointmentFormDialog / AppointmentUpdateRequest).
        Appointment appointment = appointmentService.bookAppointment(patientId, doctorId, start, end, reason, null);
        syncCreatedEvent(appointment);
        return objectMapper.writeValueAsString(AppointmentSummary.from(appointment));
    }

    @SneakyThrows
    private String cancelAppointment(JsonNode args) {
        UUID appointmentId = ToolArguments.requiredUuid(args, "appointmentId");
        String reason = ToolArguments.optionalString(args, "reason");

        Appointment appointment = appointmentService.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        String eventId = appointment.getGoogleCalendarEventId();

        Appointment cancelled = appointmentService.cancelAppointment(appointmentId, reason);
        if (eventId != null) {
            safeCalendarRun(() -> calendarIntegrationService.cancelEvent(eventId));
        }
        return objectMapper.writeValueAsString(AppointmentSummary.from(cancelled));
    }

    @SneakyThrows
    private String rescheduleAppointment(JsonNode args) {
        UUID appointmentId = ToolArguments.requiredUuid(args, "appointmentId");
        Instant newStart = ToolArguments.requiredInstant(args, "newStart");
        Instant newEnd = ToolArguments.requiredInstant(args, "newEnd");

        Appointment existing = appointmentService.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        String eventId = existing.getGoogleCalendarEventId();

        Appointment rescheduled = appointmentService.rescheduleAppointment(appointmentId, newStart, newEnd);

        if (eventId != null) {
            safeCalendarCall(() -> calendarIntegrationService.rescheduleEvent(eventId, newStart, newEnd));
        } else {
            syncCreatedEvent(rescheduled);
        }
        return objectMapper.writeValueAsString(AppointmentSummary.from(rescheduled));
    }

    private void syncCreatedEvent(Appointment appointment) {
        safeCalendarCall(() -> {
            Patient patient = appointment.getPatient();
            Doctor doctor = appointment.getDoctor();
            Optional<String> eventId = calendarIntegrationService.createEvent(
                    "Appointment: " + patient.getFullName() + " with Dr. " + doctor.getLastName(),
                    appointment.getReason(),
                    appointment.getScheduledStart(),
                    appointment.getScheduledEnd(),
                    doctor.getEmail());
            eventId.ifPresent(id -> appointmentService.markConfirmationSent(appointment.getId(), id));
            return eventId;
        });
    }

    private void safeCalendarCall(java.util.function.Supplier<Object> action) {
        try {
            action.get();
        } catch (Exception e) {
            log.warn("Google Calendar sync failed; the appointment record is still authoritative.", e);
        }
    }

    private void safeCalendarRun(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("Google Calendar sync failed; the appointment record is still authoritative.", e);
        }
    }

    private record AppointmentSummary(String id, String start, String end, String status) {
        static AppointmentSummary from(Appointment appointment) {
            return new AppointmentSummary(appointment.getId().toString(), appointment.getScheduledStart().toString(),
                    appointment.getScheduledEnd().toString(), appointment.getStatus().name());
        }
    }
}
