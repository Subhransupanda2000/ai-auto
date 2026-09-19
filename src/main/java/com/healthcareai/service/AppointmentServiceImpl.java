package com.healthcareai.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
import com.healthcareai.dto.RevenueRange;
import com.healthcareai.dto.RevenueResponse;
import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.AppointmentStatus;
import com.healthcareai.entity.Doctor;
import com.healthcareai.entity.Patient;
import com.healthcareai.entity.Tenant;
import com.healthcareai.exception.AppointmentConflictException;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.AppointmentRepository;
import com.healthcareai.repository.DoctorRepository;
import com.healthcareai.repository.PatientRepository;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<AppointmentStatus> ACTIVE_REMINDER_STATUSES =
            Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    /** Appointment-lifecycle events that trigger a patient-facing WhatsApp
     * message, gated per-tenant by {@code Tenant.whatsappNotificationsEnabled}
     * (see {@link #sendLifecycleWhatsAppNotification}). Intentionally does
     * NOT include CONFIRMED - only booking, rescheduling, cancelling, and
     * completing an appointment notify the patient. */
    private enum NotificationEvent { SCHEDULED, RESCHEDULED, CANCELLED, COMPLETED }

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TenantRepository tenantRepository;
    private final NotificationService notificationService;
    private final TenantMessageLogService tenantMessageLogService;
    private final ClinicProperties clinicProperties;

    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlot> checkAvailability(UUID doctorId, LocalDate date) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        doctorRepository.findByIdAndTenantId(doctorId, tenantId).orElseThrow(() -> ResourceNotFoundException.of("Doctor", doctorId));

        ZoneId zone = ZoneId.of(clinicProperties.timezone());
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        List<Appointment> existing = appointmentRepository.findByDoctorAndDateRange(
                tenantId, doctorId, dayStart, dayEnd, AppointmentStatus.CANCELLED);

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
    public Appointment bookAppointment(UUID patientId, UUID doctorId, Instant start, Instant end, String reason,
                                        BigDecimal consultationFee) {
        if (!end.isAfter(start)) {
            throw new BusinessRuleViolationException("Appointment end time must be after the start time.");
        }
        UUID tenantId = TenantContext.getCurrentTenantId();
        Patient patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", patientId));
        Doctor doctor = doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Doctor", doctorId));

        assertNoOverlap(doctorId, start, end, null);

        Appointment appointment = Appointment.builder()
                .tenantId(tenantId)
                .patient(patient)
                .doctor(doctor)
                .scheduledStart(start)
                .scheduledEnd(end)
                .reason(reason)
                .consultationFee(consultationFee)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Booked appointment {} for patient {} with doctor {} at {}",
                saved.getId(), patientId, doctorId, start);
        sendLifecycleWhatsAppNotification(saved, NotificationEvent.SCHEDULED);
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
        Appointment saved = appointmentRepository.save(appointment);
        sendLifecycleWhatsAppNotification(saved, NotificationEvent.RESCHEDULED);
        return saved;
    }

    @Override
    @Transactional
    public Appointment updateConsultationFee(UUID appointmentId, BigDecimal consultationFee) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorByIdAndTenantId(appointmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        appointment.setConsultationFee(consultationFee);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueResponse getRevenueSummary(RevenueRange range) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        ZoneId zone = resolveZone();
        LocalDate today = LocalDate.now(zone);

        LocalDate startDate = switch (range) {
            case TODAY -> today;
            case YESTERDAY -> today.minusDays(1);
            case LAST_7_DAYS -> today.minusDays(6);
            case LAST_MONTH -> today.minusMonths(1).plusDays(1);
            case LAST_6_MONTHS -> today.minusMonths(6).plusDays(1);
            case LAST_YEAR -> today.minusYears(1).plusDays(1);
        };
        LocalDate endDateExclusive = range == RevenueRange.YESTERDAY ? today : today.plusDays(1);

        Instant from = startDate.atStartOfDay(zone).toInstant();
        Instant to = endDateExclusive.atStartOfDay(zone).toInstant();

        BigDecimal total = appointmentRepository.sumConsultationFeeByTenantIdAndStatusAndScheduledStartBetween(
                tenantId, AppointmentStatus.COMPLETED, from, to);
        long completedCount = appointmentRepository.countByTenantIdAndStatusAndScheduledStartGreaterThanEqualAndScheduledStartLessThan(
                tenantId, AppointmentStatus.COMPLETED, from, to);

        return new RevenueResponse(range, total, completedCount, from, to);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorByIdAndTenantId(appointmentId, TenantContext.getCurrentTenantId())
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
        Appointment saved = appointmentRepository.save(appointment);
        sendLifecycleWhatsAppNotification(saved, NotificationEvent.CANCELLED);
        return saved;
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
        Appointment saved = appointmentRepository.save(appointment);
        sendLifecycleWhatsAppNotification(saved, NotificationEvent.COMPLETED);
        return saved;
    }

    @Override
    @Transactional
    public Appointment markReminderSent(UUID appointmentId) {
        // Not tenant-scoped: only ever called by AppointmentReminderScheduler,
        // a background job with no request/tenant context, for an id it just
        // read from the (intentionally cross-tenant) findDueForReminder sweep.
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        appointment.setReminderSentAt(Instant.now());
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment markConfirmationSent(UUID appointmentId, String googleCalendarEventId) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorByIdAndTenantId(appointmentId, TenantContext.getCurrentTenantId())
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
        return appointmentRepository.findWithPatientAndDoctorByIdAndTenantId(id, TenantContext.getCurrentTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPatient(UUID patientId) {
        return appointmentRepository.findByTenantIdAndPatientIdOrderByScheduledStartDesc(TenantContext.getCurrentTenantId(), patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByDoctor(UUID doctorId) {
        return appointmentRepository.findByTenantIdAndDoctorIdOrderByScheduledStartDesc(TenantContext.getCurrentTenantId(), doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAllWithPatientAndDoctor(TenantContext.getCurrentTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findDueForReminder() {
        // Intentionally cross-tenant - see AppointmentRepository.findDueForReminder.
        Instant now = Instant.now();
        Instant reminderWindowEnd = now.plus(Duration.ofHours(clinicProperties.reminderHoursBefore()));
        return appointmentRepository.findDueForReminder(now, reminderWindowEnd, ACTIVE_REMINDER_STATUSES);
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(clinicProperties.timezone());
        } catch (Exception e) {
            log.warn("Invalid clinic timezone '{}', falling back to UTC.", clinicProperties.timezone());
            return ZoneId.of("UTC");
        }
    }

    private Appointment getActiveAppointmentOrThrow(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findWithPatientAndDoctorByIdAndTenantId(appointmentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", appointmentId));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Appointment " + appointmentId + " is " + appointment.getStatus() + " and cannot be modified.");
        }
        return appointment;
    }

    private void assertNoOverlap(UUID doctorId, Instant start, Instant end, UUID excludingAppointmentId) {
        List<Appointment> overlapping = appointmentRepository.findOverlapping(
                TenantContext.getCurrentTenantId(), doctorId, start, end, AppointmentStatus.CANCELLED);
        boolean conflict = overlapping.stream().anyMatch(a -> !a.getId().equals(excludingAppointmentId));
        if (conflict) {
            throw new AppointmentConflictException(
                    "The selected time slot is no longer available for this doctor.");
        }
    }

    /** Sends the patient a WhatsApp message for one of the four
     * appointment-lifecycle events, but only if the tenant's super
     * admin-controlled {@code whatsappNotificationsEnabled} toggle is on
     * and the patient has a phone number on file. Never lets a
     * notification failure (missing tenant, WhatsApp API error, etc.)
     * propagate and roll back the appointment change that triggered it -
     * the appointment record is always authoritative. On a successful
     * send, bumps {@code Tenant.whatsappMessageCount} so super admins can
     * see how many messages have gone out for that clinic. */
    private void sendLifecycleWhatsAppNotification(Appointment appointment, NotificationEvent event) {
        try {
            Tenant tenant = tenantRepository.findById(appointment.getTenantId()).orElse(null);
            if (tenant == null || !tenant.isWhatsappNotificationsEnabled()) {
                return;
            }
            Patient patient = appointment.getPatient();
            String phoneNumber = patient.getPhoneNumber();
            if (!StringUtils.hasText(phoneNumber)) {
                log.warn("Patient {} has no phone number on file; skipping {} WhatsApp notification.",
                        patient.getId(), event);
                return;
            }
            notificationService.sendWhatsApp(phoneNumber, buildLifecycleMessage(appointment, event));
            tenantMessageLogService.recordWhatsappMessage(tenant.getId());
        } catch (Exception e) {
            log.error("Failed to send {} WhatsApp notification for appointment {}", event, appointment.getId(), e);
        }
    }

    private String buildLifecycleMessage(Appointment appointment, NotificationEvent event) {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        String when = appointment.getScheduledStart()
                .atZone(resolveZone())
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        String clinicName = clinicProperties.name();

        return switch (event) {
            case SCHEDULED -> "Hi %s, your appointment with Dr. %s at %s has been scheduled for %s."
                    .formatted(patient.getFirstName(), doctor.getLastName(), clinicName, when);
            case RESCHEDULED -> "Hi %s, your appointment with Dr. %s at %s has been rescheduled to %s."
                    .formatted(patient.getFirstName(), doctor.getLastName(), clinicName, when);
            case CANCELLED -> "Hi %s, your appointment with Dr. %s at %s scheduled for %s has been cancelled."
                    .formatted(patient.getFirstName(), doctor.getLastName(), clinicName, when);
            case COMPLETED -> "Hi %s, thank you for visiting Dr. %s at %s. Your appointment on %s has been "
                    .formatted(patient.getFirstName(), doctor.getLastName(), clinicName, when)
                    + "marked as completed. Wishing you a speedy recovery!";
        };
    }
}
