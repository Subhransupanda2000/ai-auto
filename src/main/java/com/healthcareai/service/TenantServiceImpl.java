package com.healthcareai.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.entity.Role;
import com.healthcareai.entity.Tenant;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Tenant createTenantWithAdmin(String tenantName, String slug, String adminFullName,
                                         String adminEmail, String adminPassword) {
        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            throw new BusinessRuleViolationException("A tenant with slug '" + slug + "' already exists.");
        }

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name(tenantName)
                .slug(slug)
                .active(true)
                .build());

        // The first admin user must be created "as" this brand-new tenant so
        // its tenant_id is stamped correctly, even though the super admin
        // performing this call belongs to no tenant at all.
        TenantContext.runAs(tenant.getId(), () ->
                userService.createUser(adminEmail, adminPassword, adminFullName, Role.ADMIN, tenant.getId()));

        log.info("Onboarded tenant {} ({}) with admin {}", tenant.getId(), tenant.getSlug(), adminEmail);
        return tenant;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(UUID tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @Override
    @Transactional
    public Tenant setActive(UUID tenantId, boolean active) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tenant", tenantId));
        tenant.setActive(active);
        return tenantRepository.save(tenant);
    }
}
