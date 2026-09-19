package com.healthcareai.integration;

import java.math.BigDecimal;

/**
 * Thin wrapper over the Razorpay Orders API (accessed directly over REST,
 * no SDK - same approach as {@code GeminiClient}), used by {@code
 * PaymentServiceImpl} to turn a {@code Payment} invoice into something the
 * Razorpay Checkout widget can collect, and to verify the result
 * server-side.
 */
public interface RazorpayClient {

    /**
     * Creates a Razorpay order for the given amount (in the currency's
     * major unit, e.g. rupees/dollars - converted to the minor unit
     * Razorpay expects internally) and returns the Razorpay order id to
     * hand to the Checkout widget.
     */
    RazorpayOrder createOrder(String receipt, BigDecimal amount, String currency);

    /**
     * Verifies the HMAC-SHA256 signature Checkout returns alongside a
     * successful payment, using the account's key secret. Must pass
     * before a {@code Payment} is ever marked {@code PAID} from a
     * client-supplied callback.
     */
    boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);

    /**
     * Verifies the HMAC-SHA256 signature of an incoming webhook payload
     * using the account's webhook secret (distinct from the key secret).
     */
    boolean verifyWebhookSignature(String payload, String signature);

    /** The publishable key id, handed to the frontend to open Checkout. */
    String getPublicKeyId();

    record RazorpayOrder(String id, long amountInMinorUnits, String currency) {
    }
}
