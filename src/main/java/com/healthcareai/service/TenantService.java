package com.healthcareai.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.entity.Tenant;

public interface TenantService {

    /** Creates a new tenant and its first (ADMIN) staff user in one
     * transaction. Only reachable by a super admin. */
    Tenant createTenantWithAdmin(String tenantName, String slug, String adminFullName,
                                  String adminEmail, String adminPassword);

    List<Tenant> findAll();

    Optional<Tenant> findById(UUID tenantId);

    Tenant setActive(UUID tenantId, boolean active);
}
