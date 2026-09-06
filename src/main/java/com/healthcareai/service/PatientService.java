package com.healthcareai.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.entity.Patient;

public interface PatientService {

    Patient createPatient(String firstName, String lastName, String phoneNumber, String email,
                           LocalDate dateOfBirth, String gender, String notes);

    Patient updatePatient(UUID id, String firstName, String lastName, String email, String gender, String notes);

    Optional<Patient> findById(UUID id);

    Optional<Patient> findByPhoneNumber(String phoneNumber);

    /**
     * Finds a patient by phone number, creating a minimal record if none
     * exists yet. Used by the AI receptionist when a new patient reaches
     * out over WhatsApp/SMS before any registration has taken place.
     */
    Patient findOrCreateByPhoneNumber(String phoneNumber, String fallbackFirstName);

    List<Patient> findAll();
}
