package com.healthcareai.entity;

/**
 * Lifecycle states of a {@link Payment} (an invoice raised by a super admin
 * against a tenant, settled through Razorpay).
 */
public enum PaymentStatus {
    /** Invoice raised, awaiting payment. */
    PENDING,
    /** Confirmed paid via the Razorpay webhook signature check. */
    PAID,
    /** Razorpay reported the payment attempt failed. */
    FAILED,
    /** Cancelled by a super admin before it was paid. */
    CANCELLED
}
