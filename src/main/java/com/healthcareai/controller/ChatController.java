package com.healthcareai.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.ai.agent.ReceptionistAgent;
import com.healthcareai.dto.ChatRequest;
import com.healthcareai.dto.ChatResponse;
import com.healthcareai.entity.Channel;
import com.healthcareai.entity.Patient;
import com.healthcareai.entity.Tenant;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.service.PatientService;
import com.healthcareai.service.TenantMessageLogService;
import com.healthcareai.tenant.TenantContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Entry point for the AI receptionist chat: {@code POST /api/chat}. Public
 * (no login required) so that anonymous patient-facing channels can reach
 * it, but every message is still resolved to exactly one tenant so that
 * appointment/patient lookups the AI agent makes stay scoped to that
 * clinic's data.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI receptionist chat endpoint")
public class ChatController {

    private final ReceptionistAgent receptionistAgent;
    private final PatientService patientService;
    private final TenantRepository tenantRepository;
    private final TenantMessageLogService tenantMessageLogService;

    @PostMapping
    @Operation(summary = "Send a message to the AI receptionist and receive its reply.")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        Tenant tenant = resolveTenantContext(request);
        if (!tenant.isAiChatEnabled()) {
            throw new BusinessRuleViolationException(
                    "The AI chat assistant is currently disabled for this clinic. Please contact the front desk.");
        }

        Channel channel = request.channel() != null ? request.channel() : Channel.WEB;

        UUID patientId = null;
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            Patient patient = patientService.findOrCreateByPhoneNumber(request.phoneNumber(), null);
            patientId = patient.getId();
        }

        String reply = receptionistAgent.handleUserMessage(request.sessionId(), channel, patientId, request.message());
        tenantMessageLogService.recordAiChatMessage(tenant.getId());
        return ResponseEntity.ok(new ChatResponse(request.sessionId(), reply));
    }

    /**
     * An authenticated staff caller already has a tenant set by {@code
     * JwtAuthenticationFilter} (it runs on every request regardless of
     * whether the endpoint itself requires auth), so that takes priority.
     * Otherwise this must be an anonymous channel, which has to say which
     * clinic it belongs to via {@code tenantSlug}. Either way, returns the
     * resolved {@link Tenant} so the caller can check feature toggles
     * (e.g. {@code aiChatEnabled}) without a second lookup.
     */
    private Tenant resolveTenantContext(ChatRequest request) {
        UUID currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null) {
            return tenantRepository.findById(currentTenantId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Tenant", currentTenantId));
        }
        if (request.tenantSlug() == null || request.tenantSlug().isBlank()) {
            throw new BusinessRuleViolationException(
                    "tenantSlug is required when chatting without a staff login.");
        }
        Tenant tenant = tenantRepository.findBySlugIgnoreCase(request.tenantSlug())
                .orElseThrow(() -> ResourceNotFoundException.of("Tenant", request.tenantSlug()));
        if (!tenant.isActive()) {
            throw new BusinessRuleViolationException("This clinic's account has been deactivated.");
        }
        TenantContext.setCurrentTenantId(tenant.getId());
        return tenant;
    }
}
