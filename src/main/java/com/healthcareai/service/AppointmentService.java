package com.healthcareai.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.dto.AvailableSlot;
import com.healthcareai.dto.RevenueRange;
import com.healthcareai.dto.RevenueResponse;
import com.healthcareai.entity.Appointment;

public interface AppointmentService {

    /**
     * Computes free appointment slots for a doctor on a given calendar date,
     * based on clinic working hours and the doctor's existing (non-cancelled)
     * appointments.
     */
    List<AvailableSlot> checkAvailability(UUID doctorId, LocalDate date);

    Appointment bookAppointment(UUID patientId, UUID doctorId, Instant start, Instant end, String reason,
                                 BigDecimal consultationFee);

    Appointment rescheduleAppointment(UUID appointmentId, Instant newStart, Instant newEnd);

    Appointment updateConsultationFee(UUID appointmentId, BigDecimal consultationFee);

    /** Real (not estimated) revenue - the sum of {@code consultationFee}
     * across completed appointments scheduled within the given rolling
     * date range, for the caller's tenant. */
    RevenueResponse getRevenueSummary(RevenueRange range);

    Appointment cancelAppointment(UUID appointmentId, String reason);

    Appointment confirmAppointment(UUID appointmentId);

    Appointment completeAppointment(UUID appointmentId);

    Appointment markReminderSent(UUID appointmentId);

    Appointment markConfirmationSent(UUID appointmentId, String googleCalendarEventId);

    Optional<Appointment> findById(UUID id);

    List<Appointment> findByPatient(UUID patientId);

    List<Appointment> findByDoctor(UUID doctorId);

    List<Appointment> findAll();

    List<Appointment> findDueForReminder();
}
