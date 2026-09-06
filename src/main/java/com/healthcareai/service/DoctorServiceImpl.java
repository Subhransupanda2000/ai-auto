package com.healthcareai.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.healthcareai.entity.Doctor;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public Doctor createDoctor(String firstName, String lastName, String specialty, String email,
                                String phoneNumber, String bio) {
        Doctor doctor = Doctor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .specialty(specialty)
                .email(email)
                .phoneNumber(phoneNumber)
                .bio(bio)
                .build();
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor updateDoctor(UUID id, String specialty, String email, String phoneNumber, String bio, Boolean active) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Doctor", id));
        if (StringUtils.hasText(specialty)) {
            doctor.setSpecialty(specialty);
        }
        if (StringUtils.hasText(email)) {
            doctor.setEmail(email);
        }
        if (StringUtils.hasText(phoneNumber)) {
            doctor.setPhoneNumber(phoneNumber);
        }
        if (StringUtils.hasText(bio)) {
            doctor.setBio(bio);
        }
        if (active != null) {
            doctor.setActive(active);
        }
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> findById(UUID id) {
        return doctorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> findActiveDoctors() {
        return doctorRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> findBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCaseAndActiveTrue(specialty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> search(String query) {
        return doctorRepository.search(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }
}
