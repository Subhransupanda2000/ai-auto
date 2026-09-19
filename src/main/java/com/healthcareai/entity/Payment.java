package com.healthcareai.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An invoice raised by a super admin against a clinic {@link Tenant},
 * collected through Razorpay Checkout (one-time payment, not a recurring
 * subscription). {@code amount}/{@code currency} are fixed at creation
 * time; {@code razorpayOrderId}/{@code razorpayPaymentId}/{@code
 * razorpaySignature} are populated as the clinic goes through checkout and
 * the payment is confirmed server-side via the Razorpay webhook (see
 * {@code PaymentServiceImpl}/{@code RazorpayClient}) - never trusted from
 * the browser callback alone.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Payment {

    @Id
    @Column(updatable = false, nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    /** The clinic this invoice was raised against. Set once at creation,
     * never changed. */
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** ISO 4217 currency code, e.g. {@code "INR"}, {@code "USD"}. */
    @Column(nullable = false, length = 3)
    private String currency;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Super admin email that raised this invoice, kept for audit/history
     * even if that account is later removed. */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "paid_at")
    private Instant paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
