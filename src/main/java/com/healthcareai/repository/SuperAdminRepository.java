package com.healthcareai.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.SuperAdmin;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, UUID> {

    Optional<SuperAdmin> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
