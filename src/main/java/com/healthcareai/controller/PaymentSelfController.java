package com.healthcareai.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.PaymentCheckoutResponse;
import com.healthcareai.dto.PaymentFilter;
import com.healthcareai.dto.PaymentResponse;
import com.healthcareai.dto.PaymentVerifyRequest;
import com.healthcareai.entity.Payment;
import com.healthcareai.entity.PaymentStatus;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.service.PaymentService;
import com.healthcareai.tenant.TenantContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Clinic-facing billing: viewing the caller's own tenant's invoice history
 * and paying a pending one via Razorpay Checkout. Restricted to {@code
 * ROLE_ADMIN} (see {@code SecurityConfig}'s {@code /api/tenant/**} matcher),
 * same as {@code TenantSelfController}.
 */
@RestController
@RequestMapping("/api/tenant/payments")
@RequiredArgsConstructor
@Tag(name = "Tenant Billing", description = "The caller's own clinic's invoice history and checkout flow")
public class PaymentSelfController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "List the caller's own clinic's invoices/payments, with optional filters: "
            + "status, from/to (created date range), minAmount/maxAmount.")
    public ResponseEntity<List<PaymentResponse>> list(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        PaymentFilter filter = new PaymentFilter(null, status, from, to, minAmount, maxAmount);
        return ResponseEntity.ok(paymentService.searchForTenant(tenantId, filter).stream().map(this::toResponse).toList());
    }

    @PostMapping("/{id}/checkout")
    @Operation(summary = "Create a Razorpay order for a PENDING invoice so the clinic can pay it via Checkout.")
    public ResponseEntity<PaymentCheckoutResponse> checkout(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Payment payment = paymentService.findByIdForTenant(id, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
        PaymentService.CheckoutResult result = paymentService.startCheckout(id, tenantId);
        return ResponseEntity.ok(new PaymentCheckoutResponse(
                payment.getId(), result.razorpayOrderId(), result.razorpayKeyId(),
                result.amountInMinorUnits(), result.currency(), payment.getDescription()));
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify a Razorpay Checkout success callback and mark the invoice paid. The "
            + "asynchronous webhook (POST /api/payments/webhook) is the authoritative confirmation and "
            + "will no-op if this already applied it.")
    public ResponseEntity<PaymentResponse> verify(@PathVariable UUID id, @Valid @RequestBody PaymentVerifyRequest request) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Payment payment = paymentService.confirmFromClientCallback(
                id, tenantId, request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature());
        return ResponseEntity.ok(toResponse(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getTenantId(), null, payment.getAmount(), payment.getCurrency(),
                payment.getDescription(), payment.getStatus().name(), payment.getCreatedBy(),
                payment.getPaidAt(), payment.getCreatedAt());
    }
}
