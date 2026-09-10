package com.healthcareai.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findAllByTenantId(UUID tenantId);

    long countByTenantId(UUID tenantId);

    Optional<Patient> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Patient> findByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);
}
