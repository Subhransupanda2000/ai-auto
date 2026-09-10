package com.healthcareai.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.entity.Doctor;

public interface DoctorService {

    Doctor createDoctor(String firstName, String lastName, String specialty, String email,
                         String phoneNumber, String bio, LocalTime workingHoursStart,
                         LocalTime workingHoursEnd, String workingDays);

    Doctor updateDoctor(UUID id, String specialty, String email, String phoneNumber, String bio, Boolean active,
                         LocalTime workingHoursStart, LocalTime workingHoursEnd, String workingDays);

    Optional<Doctor> findById(UUID id);

    List<Doctor> findActiveDoctors();

    List<Doctor> findBySpecialty(String specialty);

    /** Free-text search across specialty and name, used by the AI agent to resolve a doctor from natural language. */
    List<Doctor> search(String query);

    List<Doctor> findAll();
}
