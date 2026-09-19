package com.healthcareai.exception;

/**
 * Thrown when a call to the Razorpay API fails (network error, invalid/
 * missing API keys, rate limit, etc.) - see {@code RazorpayClientImpl}.
 * Deliberately distinct from {@link LlmServiceException} so a payment
 * gateway outage is never reported to the clinic as "the AI assistant is
 * unavailable".
 */
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentGatewayException(String message) {
        super(message);
    }
}
