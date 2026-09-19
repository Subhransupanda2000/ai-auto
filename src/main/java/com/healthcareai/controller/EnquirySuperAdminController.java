package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.EnquiryResponse;
import com.healthcareai.dto.UpdateEnquiryStatusRequest;
import com.healthcareai.entity.Enquiry;
import com.healthcareai.entity.EnquiryStatus;
import com.healthcareai.service.EnquiryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Super-admin view of leads raised via the public "Request a Demo" form
 * (see {@code EnquiryController}), so the sales/onboarding team can reach
 * out. Restricted to {@code ROLE_SUPER_ADMIN} (see {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/super-admin/enquiries")
@RequiredArgsConstructor
@Tag(name = "Demo Enquiries (Super Admin)", description = "Leads raised via the public request-a-demo form")
public class EnquirySuperAdminController {

    private final EnquiryService enquiryService;

    @GetMapping
    @Operation(summary = "List demo-request leads, optionally filtered by status.")
    public ResponseEntity<List<EnquiryResponse>> list(@RequestParam(required = false) EnquiryStatus status) {
        return ResponseEntity.ok(enquiryService.findAll(status).stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update a lead's follow-up status (NEW/CONTACTED/CONVERTED/CLOSED).")
    public ResponseEntity<EnquiryResponse> updateStatus(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateEnquiryStatusRequest request) {
        return ResponseEntity.ok(toResponse(enquiryService.updateStatus(id, request.status())));
    }

    private EnquiryResponse toResponse(Enquiry enquiry) {
        return new EnquiryResponse(enquiry.getId(), enquiry.getFullName(), enquiry.getEmail(), enquiry.getPhone(),
                enquiry.getClinicName(), enquiry.getMessage(), enquiry.getStatus().name(), enquiry.getCreatedAt());
    }
}
