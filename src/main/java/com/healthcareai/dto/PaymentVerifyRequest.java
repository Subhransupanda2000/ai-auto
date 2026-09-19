package com.healthcareai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Sent by the frontend immediately after Razorpay Checkout's success
 * handler fires, so the UI can reflect a paid invoice without waiting for
 * the asynchronous webhook. The signature is re-verified server-side
 * (see {@code RazorpayClient#verifyPaymentSignature}) before the {@code
 * Payment} is trusted to be paid - the webhook (see {@code
 * PaymentWebhookController}) remains the authoritative confirmation and
 * will no-op here if this already marked it paid.
 */
public record PaymentVerifyRequest(
        @NotBlank String razorpayOrderId,
        @NotBlank String razorpayPaymentId,
        @NotBlank String razorpaySignature
) {
}
