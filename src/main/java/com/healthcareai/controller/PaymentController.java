package com.healthcareai.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.CreatePaymentRequest;
import com.healthcareai.dto.PaymentFilter;
import com.healthcareai.dto.PaymentResponse;
import com.healthcareai.entity.Payment;
import com.healthcareai.entity.PaymentStatus;
import com.healthcareai.entity.Tenant;
import com.healthcareai.repository.TenantRepository;
import com.healthcareai.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

/**
 * Super-admin billing: raising invoices against clinic tenants and viewing
 * the full cross-tenant payment history. Every endpoint here is restricted
 * to {@code ROLE_SUPER_ADMIN} (see {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/super-admin/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Super-admin invoicing and cross-tenant payment history")
public class PaymentController {

    private final PaymentService paymentService;
    private final TenantRepository tenantRepository;

    @PostMapping
    @Operation(summary = "Raise a new invoice (PENDING payment) against a clinic tenant.")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request,
                                                    Authentication authentication) {
        Payment payment = paymentService.raiseInvoice(request, authentication.getName());
        return ResponseEntity.ok(toResponse(payment));
    }

    @GetMapping
    @Operation(summary = "List invoices/payments across every tenant, with optional filters: "
            + "tenantId, status, from/to (created date range), minAmount/maxAmount.")
    public ResponseEntity<List<PaymentResponse>> list(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {
        PaymentFilter filter = new PaymentFilter(tenantId, status, from, to, minAmount, maxAmount);
        return ResponseEntity.ok(paymentService.search(filter).stream().map(this::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a still-PENDING invoice.")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(paymentService.cancel(id)));
    }

    private PaymentResponse toResponse(Payment payment) {
        String tenantName = tenantRepository.findById(payment.getTenantId())
                .map(Tenant::getName)
                .orElse("(deleted tenant)");
        return new PaymentResponse(
                payment.getId(), payment.getTenantId(), tenantName, payment.getAmount(), payment.getCurrency(),
                payment.getDescription(), payment.getStatus().name(), payment.getCreatedBy(),
                payment.getPaidAt(), payment.getCreatedAt());
    }
}
