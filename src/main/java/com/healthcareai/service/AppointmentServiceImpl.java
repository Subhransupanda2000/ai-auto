package com.healthcareai.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.healthcareai.config.ClinicProperties;
import com.healthcareai.dto.AvailableSlot;
import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.AppointmentStatus;
import com.healthcareai.entity.Doctor;
import com.healthcareai.entity.Patient;
import com.healthcareai.exception.AppointmentConflictException;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.AppointmentRepository;
import com.healthcareai.repository.DoctorRepository;
import com.healthcareai.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<AppointmentStatus> ACTIVE_REMINDER_STATUSES =
            Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicProperties clinicProperties;

    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlot> checkAvailability(UUID doctorId, LocalDate date) {
        doctorRepository.findById(doctorId).orElseThrow(() -> ResourceNotFoundException.of("Doctor", doctorId));

        ZoneId zone = ZoneId.of(clinicProperties.timezone());
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        List<Appointment> existing = appointmentRepository.findByDoctorAndDateRange(
                doctorId, dayStart, dayEnd, AppointmentStatus.CANCELLED);

        Duration slotDuration = Duration.ofMinutes(clinicProperties.appointmentSlotMinutes());
        List<AvailableSlot> slots = new ArrayList<>();

        LocalDateTime cursor = LocalDateTime.of(date, clinicProperties.workingHoursStart());
        LocalDateTime closing = LocalDateTime.of(date, clinicProperties.workingHoursEnd());

        while (!cursor.plus(slotDuration).isAfter(closing)) {
            Instant slotStart = cursor.atZone(zone).toInstant();
            Instant slotEnd = cursor.plus(slotDuration).atZone(zone).toInstant();

            boolean overlaps = existing.stream().anyMatch(appointment ->
                    slotStart.isBefore(appointment.getScheduledEnd()) && slotEnd.isAfter(appointment.getScheduledStart()));

            if (!overlaps) {
                slots.add(new AvailableSlot(slotStart, slotEnd));
            }
            cursor = cursor.plus(slotDuration);
        }
        return slots;
    }

    @Override
    @Transactional
    public Appointment bookAppointment(UUID patientId, UUID doctorId, Instant start, Instant end, String reason) {
        if (!end.isAfter(start)) {
            throw new BusinessRuleViolationException("Appointment end time must be after the start time.");
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Doctor", doctorId));

        assertNoOverlap(doctorId, start, end, null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledStart(start)
                .scheduledEnd(end)
                .reason(reason)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Booked appointment {} for patient {} with doctor {} at {}",
                saved.getId(), patientId, doctorId, start);
        return saved;
    }

    @Override
    @Transactional
    public Appointment rescheduleAppointment(UUID appointmentId, Instant newStart, Instant newEnd) {
        if (!newEnd.isAfter(newStart)) {
            throw new BusinessRuleViolationException("Appointment end time must be after the start time.");
        }
        Appointment appointment = getActiveAppointmentOrThrow(appointmentId);

        assertNoOverlap(appointment.getDoctor().getId(), newStart, newEnd, appointmentId);

        appointment.setScheduledStart(newStart);
        appointment.setScheduledEnd(newEnd);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReminderSentAt(null);
        appointment.setConfirmationSentAt(null);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Appointment is already cancelled.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleViolationException("A completed appointment cannot be cancelled.");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (StringUtils.hasText(reason)) {
            String existingNotes = appointment.getNotes();
            appointment.setNotes(StringUtils.hasText(existingNotes)
                    ? existingNotes + " | Cancellation reason: " + reason
                    : "Cancellation reason: " + reason);
        }
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment confirmAppointment(UUID appointmentId) {
        Appointment appointment = getActiveAppointmentOrThrow(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment completeAppointment(UUID appointmentId) {
        Appointment appointment = getActiveAppointmentOrThrow(appointmentId);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment markReminderSent(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        appointment.setReminderSentAt(Instant.now());
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment markConfirmationSent(UUID appointmentId, String googleCalendarEventId) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        appointment.setConfirmationSentAt(Instant.now());
        if (StringUtils.hasText(googleCalendarEventId)) {
            appointment.setGoogleCalendarEventId(googleCalendarEventId);
        }
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(UUID id) {
        return appointmentRepository.findWithPatientAndDoctorById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPatient(UUID patientId) {
        return appointmentRepository.findByPatientIdOrderByScheduledStartDesc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorIdOrderByScheduledStartDesc(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAllWithPatientAndDoctor();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findDueForReminder() {
        Instant now = Instant.now();
        Instant reminderWindowEnd = now.plus(Duration.ofHours(clinicProperties.reminderHoursBefore()));
        return appointmentRepository.findDueForReminder(now, reminderWindowEnd, ACTIVE_REMINDER_STATUSES);
    }

    private Appointment getActiveAppointmentOrThrow(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Appointment " + appointmentId + " is " + appointment.getStatus() + " and cannot be modified.");
        }
        return appointment;
    }

    private void assertNoOverlap(UUID doctorId, Instant start, Instant end, UUID excludingAppointmentId) {
        List<Appointment> overlapping = appointmentRepository.findOverlapping(doctorId, start, end, AppointmentStatus.CANCELLED);
        boolean conflict = overlapping.stream().anyMatch(a -> !a.getId().equals(excludingAppointmentId));
        if (conflict) {
            throw new AppointmentConflictException(
                    "The selected time slot is no longer available for this doctor.");
        }
    }
}
