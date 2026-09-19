package com.healthcareai.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.healthcareai.entity.Payment;
import com.healthcareai.entity.PaymentStatus;

/**
 * Composable filter predicates for the payment history views (super admin:
 * every tenant; clinic admin: their own tenant only) - see
 * {@code PaymentServiceImpl#search}.
 */
public final class PaymentSpecifications {

    private PaymentSpecifications() {
    }

    public static Specification<Payment> tenantId(UUID tenantId) {
        return (root, query, cb) -> tenantId == null ? null : cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<Payment> status(PaymentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Payment> createdAfter(Instant from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Payment> createdBefore(Instant to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Payment> amountAtLeast(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Payment> amountAtMost(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("amount"), max);
    }
}
