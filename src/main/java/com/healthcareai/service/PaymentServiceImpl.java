package com.healthcareai.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcareai.dto.CreatePaymentRequest;
import com.healthcareai.dto.PaymentFilter;
import com.healthcareai.entity.Payment;
import com.healthcareai.entity.PaymentStatus;
import com.healthcareai.exception.BusinessRuleViolationException;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.integration.RazorpayClient;
import com.healthcareai.repository.PaymentRepository;
import com.healthcareai.repository.PaymentSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Payment raiseInvoice(CreatePaymentRequest request, String createdByEmail) {
        Payment payment = Payment.builder()
                .tenantId(request.tenantId())
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description())
                .status(PaymentStatus.PENDING)
                .createdBy(createdByEmail)
                .build();
        payment = paymentRepository.save(payment);
        log.info("Super admin {} raised invoice {} of {} {} against tenant {}",
                createdByEmail, payment.getId(), payment.getAmount(), payment.getCurrency(), payment.getTenantId());
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> search(PaymentFilter filter) {
        return paymentRepository.findAll(toSpecification(filter, null), org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> searchForTenant(UUID tenantId, PaymentFilter filter) {
        return paymentRepository.findAll(toSpecification(filter, tenantId), org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdForTenant(UUID id, UUID tenantId) {
        return paymentRepository.findByIdAndTenantId(id, tenantId);
    }

    @Override
    @Transactional
    public Payment cancel(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException("Only a PENDING invoice can be cancelled.");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public CheckoutResult startCheckout(UUID paymentId, UUID tenantId) {
        Payment payment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException("This invoice is not awaiting payment.");
        }
        RazorpayClient.RazorpayOrder order = razorpayClient.createOrder(
                "payment_" + payment.getId(), payment.getAmount(), payment.getCurrency());
        payment.setRazorpayOrderId(order.id());
        paymentRepository.save(payment);
        return new CheckoutResult(order.id(), razorpayClient.getPublicKeyId(), order.amountInMinorUnits(), order.currency());
    }

    @Override
    @Transactional
    public Payment confirmFromClientCallback(UUID paymentId, UUID tenantId, String razorpayOrderId,
                                              String razorpayPaymentId, String razorpaySignature) {
        Payment payment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));
        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }
        if (payment.getRazorpayOrderId() == null || !payment.getRazorpayOrderId().equals(razorpayOrderId)) {
            throw new BusinessRuleViolationException("Order id does not match this invoice.");
        }
        if (!razorpayClient.verifyPaymentSignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            log.warn("Rejected invalid Razorpay signature for payment {}", paymentId);
            throw new BusinessRuleViolationException("Payment signature verification failed.");
        }
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        payment = paymentRepository.save(payment);
        log.info("Payment {} confirmed paid via client callback", paymentId);
        return payment;
    }

    @Override
    @Transactional
    public void handleWebhook(String rawPayload, String signatureHeader) {
        if (signatureHeader == null || !razorpayClient.verifyWebhookSignature(rawPayload, signatureHeader)) {
            log.warn("Rejected Razorpay webhook with invalid signature.");
            throw new BusinessRuleViolationException("Invalid webhook signature.");
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(rawPayload);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Malformed webhook payload.");
        }
        String event = root.path("event").asText("");
        JsonNode entity = root.path("payload").path("payment").path("entity");
        String orderId = entity.path("order_id").asText(null);
        String razorpayPaymentId = entity.path("id").asText(null);
        if (orderId == null) {
            log.info("Ignoring Razorpay webhook event {} with no order id", event);
            return;
        }
        paymentRepository.findByRazorpayOrderId(orderId).ifPresentOrElse(payment -> {
            if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.CANCELLED) {
                return;
            }
            if ("payment.captured".equals(event)) {
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(Instant.now());
                paymentRepository.save(payment);
                log.info("Payment {} confirmed paid via webhook", payment.getId());
            } else if ("payment.failed".equals(event)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.info("Payment {} marked failed via webhook", payment.getId());
            }
        }, () -> log.warn("Received Razorpay webhook for unknown order id {}", orderId));
    }

    private Specification<Payment> toSpecification(PaymentFilter filter, UUID forcedTenantId) {
        UUID tenantId = forcedTenantId != null ? forcedTenantId : (filter != null ? filter.tenantId() : null);
        if (filter == null) {
            return Specification.allOf(PaymentSpecifications.tenantId(tenantId));
        }
        return Specification.allOf(
                PaymentSpecifications.tenantId(tenantId),
                PaymentSpecifications.status(filter.status()),
                PaymentSpecifications.createdAfter(filter.from()),
                PaymentSpecifications.createdBefore(filter.to()),
                PaymentSpecifications.amountAtLeast(filter.minAmount()),
                PaymentSpecifications.amountAtMost(filter.maxAmount()));
    }
}
