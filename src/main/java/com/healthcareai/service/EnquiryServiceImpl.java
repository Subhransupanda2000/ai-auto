package com.healthcareai.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.config.DemoProperties;
import com.healthcareai.dto.CreateEnquiryRequest;
import com.healthcareai.entity.Enquiry;
import com.healthcareai.entity.EnquiryStatus;
import com.healthcareai.entity.Tenant;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.EnquiryRepository;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.security.demo.DemoJwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final TenantRepository tenantRepository;
    private final DemoJwtService demoJwtService;
    private final DemoProperties demoProperties;

    @Override
    @Transactional
    public SignupResult create(CreateEnquiryRequest request) {
        Enquiry enquiry = enquiryRepository.save(Enquiry.builder()
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .clinicName(request.clinicName())
                .message(request.message())
                .status(EnquiryStatus.NEW)
                .build());

        Tenant demoTenant = tenantRepository.findBySlugIgnoreCase(demoProperties.tenantSlug())
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "No demo tenant is configured (expected slug '" + demoProperties.tenantSlug() + "')."));

        String demoToken = demoJwtService.generateDemoAccessToken(
                "demo-" + enquiry.getId() + "@preview.healthcareai", demoTenant.getId(), demoTenant.getName());

        log.info("New demo enquiry {} from {} <{}> - issued a read-only demo session over tenant {}",
                enquiry.getId(), enquiry.getFullName(), enquiry.getEmail(), demoTenant.getSlug());

        return new SignupResult(enquiry, demoToken, demoJwtService.getAccessTokenTtlSeconds());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enquiry> findAll(EnquiryStatus status) {
        return status == null
                ? enquiryRepository.findAllByOrderByCreatedAtDesc()
                : enquiryRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional
    public Enquiry updateStatus(UUID id, EnquiryStatus status) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Enquiry", id));
        enquiry.setStatus(status);
        return enquiryRepository.save(enquiry);
    }
}
