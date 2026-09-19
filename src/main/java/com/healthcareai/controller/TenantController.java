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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.CreateTenantRequest;
import com.healthcareai.dto.MessageStatsRange;
import com.healthcareai.dto.TenantAdminPasswordResetResponse;
import com.healthcareai.dto.TenantMessageStatsResponse;
import com.healthcareai.dto.TenantResponse;
import com.healthcareai.dto.UpdateTenantAiChatRequest;
import com.healthcareai.dto.UpdateTenantStatusRequest;
import com.healthcareai.dto.UpdateTenantWhatsappRequest;
import com.healthcareai.entity.Tenant;
import com.healthcareai.service.TenantMessageLogService;
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
    private final TenantMessageLogService tenantMessageLogService;

    @PostMapping
    @Operation(summary = "Onboard a new clinic tenant along with its first ADMIN user.")
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantService.createTenantWithAdmin(
                request.tenantName(), request.slug(), request.adminFullName(),
                request.adminEmail(), request.adminPassword());
        return ResponseEntity.ok(toResponse(tenant));
    }

    @GetMapping
    @Operation(summary = "List all tenants. Optionally report each tenant's whatsappMessageCount/"
            + "aiChatMessageCount for THIS_MONTH or LAST_MONTH instead of their lifetime totals "
            + "(default ALL_TIME) - drives the message-count-window dropdown on the Tenants page.")
    public ResponseEntity<List<TenantResponse>> list(
            @RequestParam(defaultValue = "ALL_TIME") MessageStatsRange range) {
        return ResponseEntity.ok(tenantService.findAll().stream().map(t -> toResponse(t, range)).toList());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Activate or deactivate a tenant.")
    public ResponseEntity<TenantResponse> updateStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateTenantStatusRequest request) {
        Tenant tenant = tenantService.setActive(id, request.active());
        return ResponseEntity.ok(toResponse(tenant));
    }

    @PatchMapping("/{id}/whatsapp")
    @Operation(summary = "Enable or disable appointment-lifecycle WhatsApp messages "
            + "(scheduled/rescheduled/cancelled/completed) for this clinic's patients.")
    public ResponseEntity<TenantResponse> updateWhatsappNotifications(@PathVariable UUID id,
                                                                        @Valid @RequestBody UpdateTenantWhatsappRequest request) {
        Tenant tenant = tenantService.setWhatsappNotificationsEnabled(id, request.enabled());
        return ResponseEntity.ok(toResponse(tenant));
    }

    @PatchMapping("/{id}/ai-chat")
    @Operation(summary = "Enable or disable the AI receptionist chat (POST /api/chat) for this clinic.")
    public ResponseEntity<TenantResponse> updateAiChat(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateTenantAiChatRequest request) {
        Tenant tenant = tenantService.setAiChatEnabled(id, request.enabled());
        return ResponseEntity.ok(toResponse(tenant));
    }

    @PostMapping("/{id}/reset-admin-password")
    @Operation(summary = "Generate a new temporary password for this clinic's admin account and set it "
            + "immediately, returned once in this response (not emailed, not stored in plaintext) so the "
            + "super admin can relay it to the clinic directly. Use this to recover a locked-out clinic "
            + "admin when SMTP/email isn't configured for the self-service forgot-password flow.")
    public ResponseEntity<TenantAdminPasswordResetResponse> resetAdminPassword(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.resetTenantAdminPassword(id));
    }

    private TenantResponse toResponse(Tenant tenant) {
        return toResponse(tenant, MessageStatsRange.ALL_TIME);
    }

    private TenantResponse toResponse(Tenant tenant, MessageStatsRange range) {
        long whatsappCount = tenant.getWhatsappMessageCount();
        long aiChatCount = tenant.getAiChatMessageCount();
        if (range == MessageStatsRange.THIS_MONTH || range == MessageStatsRange.LAST_MONTH) {
            TenantMessageStatsResponse stats = tenantMessageLogService.getStats(tenant.getId(), range);
            whatsappCount = stats.whatsappMessageCount();
            aiChatCount = stats.aiChatMessageCount();
        }
        return new TenantResponse(
                tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.isActive(),
                userService.countByTenant(tenant.getId()), tenant.getCreatedAt(),
                tenant.isWhatsappNotificationsEnabled(), whatsappCount,
                tenant.isAiChatEnabled(), aiChatCount);
    }
}
