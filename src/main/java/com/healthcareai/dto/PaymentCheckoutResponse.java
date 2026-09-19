package com.healthcareai.dto;

import java.util.UUID;

/**
 * Everything the frontend needs to open Razorpay Checkout for a pending
 * {@code Payment}: the Razorpay order just created for it, the amount in
 * the currency's minor unit (paise/cents/...) Razorpay expects, and the
 * account's public key id (never the secret).
 */
public record PaymentCheckoutResponse(
        UUID paymentId,
        String razorpayOrderId,
        String razorpayKeyId,
        long amountInMinorUnits,
        String currency,
        String description
) {
}
