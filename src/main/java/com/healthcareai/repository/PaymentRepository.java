package com.healthcareai.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.healthcareai.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
