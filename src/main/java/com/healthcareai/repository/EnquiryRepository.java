package com.healthcareai.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.Enquiry;
import com.healthcareai.entity.EnquiryStatus;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {

    List<Enquiry> findAllByOrderByCreatedAtDesc();

    List<Enquiry> findByStatusOrderByCreatedAtDesc(EnquiryStatus status);
}
