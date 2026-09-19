package com.healthcareai.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Super-admin request to raise a new invoice (a {@code PENDING} {@code
 * Payment}) against a clinic tenant.
 */
public record CreatePaymentRequest(
        @NotNull UUID tenantId,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO 4217 code, e.g. 'INR'.")
        String currency,

        @Size(max = 500) String description
) {
}
