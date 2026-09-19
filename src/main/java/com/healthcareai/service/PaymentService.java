package com.healthcareai.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.dto.CreatePaymentRequest;
import com.healthcareai.dto.PaymentFilter;
import com.healthcareai.entity.Payment;

public interface PaymentService {

    /** Super-admin-only: raises a new PENDING invoice against a tenant. */
    Payment raiseInvoice(CreatePaymentRequest request, String createdByEmail);

    /** Super-admin history view - every tenant, filterable. */
    List<Payment> search(PaymentFilter filter);

    /** Clinic-admin history view - forced to the caller's own tenant. */
    List<Payment> searchForTenant(UUID tenantId, PaymentFilter filter);

    Optional<Payment> findByIdForTenant(UUID id, UUID tenantId);

    /** Super-admin-only: cancels a still-PENDING invoice. */
    Payment cancel(UUID id);

    /** Creates a Razorpay order for a PENDING invoice belonging to
     * {@code tenantId} so the clinic can pay it via Checkout. */
    CheckoutResult startCheckout(UUID paymentId, UUID tenantId);

    /** Verifies the Checkout success signature and marks the payment PAID
     * if valid; idempotent so the later webhook confirmation is a no-op. */
    Payment confirmFromClientCallback(UUID paymentId, UUID tenantId, String razorpayOrderId,
                                       String razorpayPaymentId, String razorpaySignature);

    /** Verifies and applies an incoming Razorpay webhook payload. */
    void handleWebhook(String rawPayload, String signatureHeader);

    record CheckoutResult(String razorpayOrderId, String razorpayKeyId, long amountInMinorUnits, String currency) {
    }
}
