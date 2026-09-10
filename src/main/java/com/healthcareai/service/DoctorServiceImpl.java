package com.healthcareai.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.healthcareai.entity.Doctor;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.DoctorRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public Doctor createDoctor(String firstName, String lastName, String specialty, String email,
                                String phoneNumber, String bio, LocalTime workingHoursStart,
                                LocalTime workingHoursEnd, String workingDays) {
        Doctor.DoctorBuilder builder = Doctor.builder()
                .tenantId(TenantContext.getCurrentTenantId())
                .firstName(firstName)
                .lastName(lastName)
                .specialty(specialty)
                .email(email)
                .phoneNumber(phoneNumber)
                .bio(bio);
        if (workingHoursStart != null) {
            builder.workingHoursStart(workingHoursStart);
        }
        if (workingHoursEnd != null) {
            builder.workingHoursEnd(workingHoursEnd);
        }
        if (StringUtils.hasText(workingDays)) {
            builder.workingDays(workingDays);
        }
        return doctorRepository.save(builder.build());
    }

    @Override
    @Transactional
    public Doctor updateDoctor(UUID id, String specialty, String email, String phoneNumber, String bio, Boolean active,
                                LocalTime workingHoursStart, LocalTime workingHoursEnd, String workingDays) {
        Doctor doctor = doctorRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
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
        if (workingHoursStart != null) {
            doctor.setWorkingHoursStart(workingHoursStart);
        }
        if (workingHoursEnd != null) {
            doctor.setWorkingHoursEnd(workingHoursEnd);
        }
        if (StringUtils.hasText(workingDays)) {
            doctor.setWorkingDays(workingDays);
        }
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> findById(UUID id) {
        return doctorRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> findActiveDoctors() {
        return doctorRepository.findByTenantIdAndActiveTrue(TenantContext.getCurrentTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> findBySpecialty(String specialty) {
        return doctorRepository.findByTenantIdAndSpecialtyIgnoreCaseAndActiveTrue(TenantContext.getCurrentTenantId(), specialty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> search(String query) {
        return doctorRepository.search(TenantContext.getCurrentTenantId(), query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> findAll() {
        return doctorRepository.findAllByTenantId(TenantContext.getCurrentTenantId());
    }
}
