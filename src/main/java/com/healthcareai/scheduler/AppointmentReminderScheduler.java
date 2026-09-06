package com.healthcareai.scheduler;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.healthcareai.config.ClinicProperties;
import com.healthcareai.entity.Appointment;
import com.healthcareai.entity.Patient;
import com.healthcareai.service.AppointmentService;
import com.healthcareai.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Periodically finds appointments approaching their reminder window (see
 * {@code app.clinic.reminder-hours-before}) and sends a reminder to the
 * patient over WhatsApp (falling back to email if no phone number is on
 * file), then marks the appointment so it is not reminded again.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentService appointmentService;
    private final NotificationService notificationService;
    private final ClinicProperties clinicProperties;

    @Scheduled(fixedDelayString = "${app.scheduler.reminder-check-interval-ms:900000}")
    public void sendDueReminders() {
        List<Appointment> due = appointmentService.findDueForReminder();
        if (due.isEmpty()) {
            return;
        }
        log.info("Sending {} appointment reminder(s).", due.size());
        for (Appointment appointment : due) {
            try {
                sendReminder(appointment);
                appointmentService.markReminderSent(appointment.getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment {}", appointment.getId(), e);
            }
        }
    }

    private void sendReminder(Appointment appointment) {
        Patient patient = appointment.getPatient();
        String when = appointment.getScheduledStart()
                .atZone(ZoneId.of(clinicProperties.timezone()))
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));

        String message = "Hi %s, this is a reminder of your appointment with Dr. %s at %s on %s. Reply to this "
                .formatted(patient.getFirstName(), appointment.getDoctor().getLastName(), clinicProperties.name(), when)
                + "message if you need to reschedule or cancel.";

        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isBlank()) {
            notificationService.sendWhatsApp(patient.getPhoneNumber(), message);
        } else if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            notificationService.sendEmail(patient.getEmail(), "Appointment Reminder", message);
        } else {
            log.warn("Patient {} has no phone number or email on file; cannot send reminder.", patient.getId());
        }
    }
}
