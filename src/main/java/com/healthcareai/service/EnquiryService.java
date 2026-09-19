package com.healthcareai.service;

import java.util.List;
import java.util.UUID;

import com.healthcareai.dto.CreateEnquiryRequest;
import com.healthcareai.entity.Enquiry;
import com.healthcareai.entity.EnquiryStatus;

public interface EnquiryService {

    /** Records the lead and issues a fresh read-only demo access token
     * scoped to the seeded demo tenant, in one shot. */
    SignupResult create(CreateEnquiryRequest request);

    List<Enquiry> findAll(EnquiryStatus status);

    Enquiry updateStatus(UUID id, EnquiryStatus status);

    record SignupResult(Enquiry enquiry, String demoAccessToken, long expiresInSeconds) {
    }
}
