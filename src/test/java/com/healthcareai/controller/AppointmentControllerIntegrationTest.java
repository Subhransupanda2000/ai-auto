package com.healthcareai.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.healthcareai.AbstractIntegrationTest;
import com.healthcareai.dto.AppointmentRequest;
import com.healthcareai.dto.AppointmentResponse;
import com.healthcareai.dto.AppointmentUpdateRequest;
import com.healthcareai.dto.LoginRequest;
import com.healthcareai.dto.LoginResponse;
import com.healthcareai.dto.RegisterRequest;
import com.healthcareai.entity.AppointmentStatus;
import com.healthcareai.entity.Doctor;
import com.healthcareai.entity.Patient;
import com.healthcareai.entity.Role;
import com.healthcareai.service.DoctorService;
import com.healthcareai.service.PatientService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of the appointment booking API against a real
 * PostgreSQL + pgvector database (see {@link AbstractIntegrationTest}).
 */
class AppointmentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PatientService patientService;
    @Autowired
    private DoctorService doctorService;

    private Patient patient;
    private Doctor doctor;
    private String accessToken;

    @BeforeEach
    void setUp() {
        patient = patientService.createPatient("Jane", "Doe", "+15550001111", "jane@example.com", null, null, null);
        doctor = doctorService.createDoctor("John", "Smith", "Dermatology", "john.smith@example.com", null, null);
        accessToken = registerAdminAndLogin();
    }

    private String registerAdminAndLogin() {
        String uniqueEmail = "admin+" + System.nanoTime() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest(uniqueEmail, "SuperSecret123", "Admin User", Role.ADMIN);
        restTemplate.postForEntity("/api/auth/register", registerRequest, Object.class);

        LoginRequest loginRequest = new LoginRequest(uniqueEmail, "SuperSecret123");
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return loginResponse.getBody().accessToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @Test
    void fullAppointmentLifecycle_bookRescheduleAndCancel() {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        AppointmentRequest createRequest = new AppointmentRequest(patient.getId(), doctor.getId(), start, end, "Checkup");

        ResponseEntity<AppointmentResponse> createResponse = restTemplate.exchange(
                "/api/appointments", HttpMethod.POST, new HttpEntity<>(createRequest, authHeaders()), AppointmentResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AppointmentResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.status()).isEqualTo(AppointmentStatus.SCHEDULED.name());
        assertThat(created.patientId()).isEqualTo(patient.getId());

        ResponseEntity<AppointmentResponse[]> listResponse = restTemplate.exchange(
                "/api/appointments?patientId=" + patient.getId(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), AppointmentResponse[].class);
        assertThat(listResponse.getBody()).extracting(AppointmentResponse::id).contains(created.id());

        Instant newStart = start.plus(1, ChronoUnit.HOURS);
        Instant newEnd = newStart.plus(30, ChronoUnit.MINUTES);
        AppointmentUpdateRequest rescheduleRequest = new AppointmentUpdateRequest(newStart, newEnd, null, null);
        ResponseEntity<AppointmentResponse> rescheduleResponse = restTemplate.exchange(
                "/api/appointments/" + created.id(), HttpMethod.PUT,
                new HttpEntity<>(rescheduleRequest, authHeaders()), AppointmentResponse.class);
        assertThat(rescheduleResponse.getBody().scheduledStart()).isEqualTo(newStart);

        ResponseEntity<AppointmentResponse> cancelResponse = restTemplate.exchange(
                "/api/appointments/" + created.id(), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), AppointmentResponse.class);
        assertThat(cancelResponse.getBody().status()).isEqualTo(AppointmentStatus.CANCELLED.name());
    }

    @Test
    void bookingOverlappingSlot_returnsConflict() {
        Instant start = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        AppointmentRequest request = new AppointmentRequest(patient.getId(), doctor.getId(), start, end, "Checkup");

        restTemplate.exchange("/api/appointments", HttpMethod.POST, new HttpEntity<>(request, authHeaders()), AppointmentResponse.class);
        ResponseEntity<Object> conflictResponse = restTemplate.exchange(
                "/api/appointments", HttpMethod.POST, new HttpEntity<>(request, authHeaders()), Object.class);

        assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
