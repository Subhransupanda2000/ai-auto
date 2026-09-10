package com.healthcareai.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.CreateTenantRequest;
import com.healthcareai.dto.TenantResponse;
import com.healthcareai.dto.UpdateTenantStatusRequest;
import com.healthcareai.entity.Tenant;
import com.healthcareai.service.TenantService;
import com.healthcareai.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Tenant (clinic) onboarding and management. Every endpoint here is
 * restricted to {@code ROLE_SUPER_ADMIN} (see {@code SecurityConfig}) -
 * onboarding a new clinic is exclusively a super-admin action.
 */
@RestController
@RequestMapping("/api/super-admin/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Super-admin-only tenant onboarding/management")
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Onboard a new clinic tenant along with its first ADMIN user.")
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantService.createTenantWithAdmin(
                request.tenantName(), request.slug(), request.adminFullName(),
                request.adminEmail(), request.adminPassword());
        return ResponseEntity.ok(toResponse(tenant));
    }

    @GetMapping
    @Operation(summary = "List all tenants.")
    public ResponseEntity<List<TenantResponse>> list() {
        return ResponseEntity.ok(tenantService.findAll().stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Activate or deactivate a tenant.")
    public ResponseEntity<TenantResponse> updateStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateTenantStatusRequest request) {
        Tenant tenant = tenantService.setActive(id, request.active());
        return ResponseEntity.ok(toResponse(tenant));
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.isActive(),
                userService.countByTenant(tenant.getId()), tenant.getCreatedAt());
    }
}
