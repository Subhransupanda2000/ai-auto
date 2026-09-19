package com.healthcareai.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.MessageStatsRange;
import com.healthcareai.dto.TenantMessageStatsResponse;
import com.healthcareai.dto.TenantSelfResponse;
import com.healthcareai.entity.Tenant;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.service.TenantMessageLogService;
import com.healthcareai.tenant.TenantContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Read-only self-service info about the caller's own clinic. Restricted to
 * {@code ROLE_ADMIN} (see {@code SecurityConfig}) - lets a clinic admin see
 * the WhatsApp/AI-chat feature toggles a super admin has set for them and
 * how many messages have gone out, without granting access to any other
 * tenant's data or to the toggles themselves (those stay super-admin-only,
 * see {@link TenantController}).
 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
@Tag(name = "Tenant Self-Service", description = "Read-only info about the caller's own clinic")
public class TenantSelfController {

    private final TenantRepository tenantRepository;
    private final TenantMessageLogService tenantMessageLogService;

    @GetMapping("/me")
    @Operation(summary = "Get the caller's own clinic's status and feature usage counts.")
    public ResponseEntity<TenantSelfResponse> me() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tenant", tenantId));
        return ResponseEntity.ok(new TenantSelfResponse(
                tenant.getName(), tenant.getSlug(), tenant.isActive(),
                tenant.isWhatsappNotificationsEnabled(), tenant.getWhatsappMessageCount(),
                tenant.isAiChatEnabled(), tenant.getAiChatMessageCount()));
    }

    @GetMapping("/me/message-stats")
    @Operation(summary = "WhatsApp/AI-chat message counts for the caller's own clinic - ALL_TIME "
            + "(default), THIS_MONTH, or LAST_MONTH.")
    public ResponseEntity<TenantMessageStatsResponse> messageStats(
            @RequestParam(defaultValue = "ALL_TIME") MessageStatsRange range) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return ResponseEntity.ok(tenantMessageLogService.getStats(tenantId, range));
    }
}
