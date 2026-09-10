package com.healthcareai.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.healthcareai.entity.Patient;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.PatientRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public Patient createPatient(String firstName, String lastName, String phoneNumber, String email,
                                  LocalDate dateOfBirth, String gender, String notes) {
        Patient patient = Patient.builder()
                .tenantId(TenantContext.getCurrentTenantId())
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .email(email)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .notes(notes)
                .build();
        Patient saved = patientRepository.save(patient);
        log.info("Created patient {} ({})", saved.getId(), saved.getPhoneNumber());
        return saved;
    }

    @Override
    @Transactional
    public Patient updatePatient(UUID id, String firstName, String lastName, String email, String gender, String notes) {
        Patient patient = patientRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", id));
        if (StringUtils.hasText(firstName)) {
            patient.setFirstName(firstName);
        }
        if (StringUtils.hasText(lastName)) {
            patient.setLastName(lastName);
        }
        if (StringUtils.hasText(email)) {
            patient.setEmail(email);
        }
        if (StringUtils.hasText(gender)) {
            patient.setGender(gender);
        }
        if (StringUtils.hasText(notes)) {
            patient.setNotes(notes);
        }
        return patientRepository.save(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findById(UUID id) {
        return patientRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findByPhoneNumber(String phoneNumber) {
        return patientRepository.findByTenantIdAndPhoneNumber(TenantContext.getCurrentTenantId(), phoneNumber);
    }

    @Override
    @Transactional
    public Patient findOrCreateByPhoneNumber(String phoneNumber, String fallbackFirstName) {
        return patientRepository.findByTenantIdAndPhoneNumber(TenantContext.getCurrentTenantId(), phoneNumber)
                .orElseGet(() -> createPatient(
                        StringUtils.hasText(fallbackFirstName) ? fallbackFirstName : "Unknown",
                        "Patient",
                        phoneNumber,
                        null,
                        null,
                        null,
                        null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAllByTenantId(TenantContext.getCurrentTenantId());
    }
}
