//package com.healthcareai.service;
//
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.healthcareai.config.ClinicProperties;
//import com.healthcareai.dto.AvailableSlot;
//import com.healthcareai.entity.Appointment;
//import com.healthcareai.entity.AppointmentStatus;
//import com.healthcareai.entity.Doctor;
//import com.healthcareai.entity.Patient;
//import com.healthcareai.exception.AppointmentConflictException;
//import com.healthcareai.exception.BusinessRuleViolationException;
//import com.healthcareai.exception.ResourceNotFoundException;
//import com.healthcareai.repository.AppointmentRepository;
//import com.healthcareai.repository.DoctorRepository;
//import com.healthcareai.repository.PatientRepository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AppointmentServiceImplTest {
//
//    @Mock
//    private AppointmentRepository appointmentRepository;
//    @Mock
//    private PatientRepository patientRepository;
//    @Mock
//    private DoctorRepository doctorRepository;
//
//    private AppointmentServiceImpl appointmentService;
//
//    private Patient patient;
//    private Doctor doctor;
//
//    @BeforeEach
//    void setUp() {
//        ClinicProperties clinicProperties = new ClinicProperties(
//                "Test Clinic", "UTC", LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 24, null, null);
//        appointmentService = new AppointmentServiceImpl(appointmentRepository, patientRepository, doctorRepository, clinicProperties);
//
//        patient = Patient.builder().id(UUID.randomUUID()).firstName("Jane").lastName("Doe")
//                .phoneNumber("+15551234567").build();
//        doctor = Doctor.builder().id(UUID.randomUUID()).firstName("John").lastName("Smith")
//                .specialty("Dermatology").build();
//    }
//
//    @Test
//    void bookAppointment_savesAppointment_whenNoConflict() {
//        Instant start = Instant.parse("2030-01-01T10:00:00Z");
//        Instant end = start.plus(30, ChronoUnit.MINUTES);
//
//        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
//        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
//        when(appointmentRepository.findOverlapping(eq(doctor.getId()), eq(start), eq(end), eq(AppointmentStatus.CANCELLED)))
//                .thenReturn(List.of());
//        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Appointment result = appointmentService.bookAppointment(patient.getId(), doctor.getId(), start, end, "Checkup");
//
//        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
//        assertThat(result.getPatient()).isEqualTo(patient);
//        assertThat(result.getDoctor()).isEqualTo(doctor);
//        verify(appointmentRepository).save(any(Appointment.class));
//    }
//
//    @Test
//    void bookAppointment_throwsConflict_whenOverlappingAppointmentExists() {
//        Instant start = Instant.parse("2030-01-01T10:00:00Z");
//        Instant end = start.plus(30, ChronoUnit.MINUTES);
//
//        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
//        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
//        Appointment existing = Appointment.builder().id(UUID.randomUUID()).build();
//        when(appointmentRepository.findOverlapping(eq(doctor.getId()), eq(start), eq(end), eq(AppointmentStatus.CANCELLED)))
//                .thenReturn(List.of(existing));
//
//        assertThatThrownBy(() -> appointmentService.bookAppointment(patient.getId(), doctor.getId(), start, end, "Checkup"))
//                .isInstanceOf(AppointmentConflictException.class);
//
//        verify(appointmentRepository, never()).save(any());
//    }
//
//    @Test
//    void bookAppointment_throwsNotFound_whenPatientMissing() {
//        Instant start = Instant.parse("2030-01-01T10:00:00Z");
//        Instant end = start.plus(30, ChronoUnit.MINUTES);
//        when(patientRepository.findById(patient.getId())).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> appointmentService.bookAppointment(patient.getId(), doctor.getId(), start, end, null))
//                .isInstanceOf(ResourceNotFoundException.class);
//    }
//
//    @Test
//    void cancelAppointment_throwsBusinessRuleViolation_whenAlreadyCancelled() {
//        UUID appointmentId = UUID.randomUUID();
//        Appointment cancelled = Appointment.builder().id(appointmentId).status(AppointmentStatus.CANCELLED).build();
//        when(appointmentRepository.findWithPatientAndDoctorById(appointmentId)).thenReturn(Optional.of(cancelled));
//
//        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointmentId, "no longer needed"))
//                .isInstanceOf(BusinessRuleViolationException.class);
//    }
//
//    @Test
//    void checkAvailability_excludesSlotsOverlappingExistingAppointments() {
//        LocalDate date = LocalDate.of(2030, 1, 2);
//        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
//
//        Instant bookedStart = date.atTime(9, 0).atZone(java.time.ZoneOffset.UTC).toInstant();
//        Instant bookedEnd = bookedStart.plus(30, ChronoUnit.MINUTES);
//        Appointment booked = Appointment.builder()
//                .scheduledStart(bookedStart).scheduledEnd(bookedEnd).status(AppointmentStatus.SCHEDULED).build();
//
//        when(appointmentRepository.findByDoctorAndDateRange(eq(doctor.getId()), any(), any(), eq(AppointmentStatus.CANCELLED)))
//                .thenReturn(List.of(booked));
//
//        List<AvailableSlot> slots = appointmentService.checkAvailability(doctor.getId(), date);
//
//        assertThat(slots).isNotEmpty();
//        assertThat(slots).noneMatch(slot -> slot.start().equals(bookedStart));
//    }
//}
