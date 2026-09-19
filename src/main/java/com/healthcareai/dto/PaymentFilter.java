package com.healthcareai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.healthcareai.entity.PaymentStatus;

/**
 * Optional filters for the payment history views - every field left
 * {@code null} is simply not applied (see {@code PaymentSpecifications}).
 * {@code tenantId} is only honoured on the super-admin listing; the
 * clinic-facing listing always forces it to the caller's own tenant
 * regardless of what's passed in.
 */
public record PaymentFilter(
        UUID tenantId,
        PaymentStatus status,
        Instant from,
        Instant to,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
}
