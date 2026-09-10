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

    @PostMapping
    @Operation(summary = "Send a message to the AI receptionist and receive its reply.")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        resolveTenantContext(request);

        Channel channel = request.channel() != null ? request.channel() : Channel.WEB;

        UUID patientId = null;
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            Patient patient = patientService.findOrCreateByPhoneNumber(request.phoneNumber(), null);
            patientId = patient.getId();
        }

        String reply = receptionistAgent.handleUserMessage(request.sessionId(), channel, patientId, request.message());
        return ResponseEntity.ok(new ChatResponse(request.sessionId(), reply));
    }

    /**
     * An authenticated staff caller already has a tenant set by {@code
     * JwtAuthenticationFilter} (it runs on every request regardless of
     * whether the endpoint itself requires auth), so that takes priority.
     * Otherwise this must be an anonymous channel, which has to say which
     * clinic it belongs to via {@code tenantSlug}.
     */
    private void resolveTenantContext(ChatRequest request) {
        if (TenantContext.getCurrentTenantId() != null) {
            return;
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
    }
}
