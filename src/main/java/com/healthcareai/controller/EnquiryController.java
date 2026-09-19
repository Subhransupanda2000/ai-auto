package com.healthcareai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.CreateEnquiryRequest;
import com.healthcareai.dto.DemoSignupResponse;
import com.healthcareai.dto.EnquiryResponse;
import com.healthcareai.entity.Enquiry;
import com.healthcareai.service.EnquiryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Public "Request a Demo" signup: anyone can leave their contact details
 * (see {@code SecurityConfig}'s permitAll for this path) and is
 * immediately handed a short-lived, read-only demo session over the
 * seeded demo tenant - no account, no password. The lead itself is
 * visible to super admins at {@code GET /api/super-admin/enquiries}.
 */
@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
@Tag(name = "Demo Enquiries", description = "Public request-a-demo signup")
public class EnquiryController {

    private final EnquiryService enquiryService;

    @PostMapping
    @Operation(summary = "Submit a request-a-demo lead and receive a read-only demo session token.")
    public ResponseEntity<DemoSignupResponse> create(@Valid @RequestBody CreateEnquiryRequest request) {
        EnquiryService.SignupResult result = enquiryService.create(request);
        return ResponseEntity.ok(DemoSignupResponse.of(toResponse(result.enquiry()), result.demoAccessToken(),
                result.expiresInSeconds()));
    }

    private EnquiryResponse toResponse(Enquiry enquiry) {
        return new EnquiryResponse(enquiry.getId(), enquiry.getFullName(), enquiry.getEmail(), enquiry.getPhone(),
                enquiry.getClinicName(), enquiry.getMessage(), enquiry.getStatus().name(), enquiry.getCreatedAt());
    }
}
