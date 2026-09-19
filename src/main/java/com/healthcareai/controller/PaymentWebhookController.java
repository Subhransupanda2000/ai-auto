package com.healthcareai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcareai.dto.MessageResponse;
import com.healthcareai.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Razorpay webhook endpoint - the authoritative, server-to-server
 * confirmation that a payment succeeded/failed. Deliberately public (see
 * {@code SecurityConfig}): Razorpay cannot present a staff/super-admin JWT,
 * so this is instead secured by verifying the {@code X-Razorpay-Signature}
 * header against the configured webhook secret (see {@code
 * RazorpayClient#verifyWebhookSignature}) before trusting the payload at
 * all.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments Webhook", description = "Public Razorpay webhook receiver, secured by signature verification")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay webhook receiver. Configure this URL (https://<host>/api/payments/webhook) "
            + "in the Razorpay Dashboard's Webhooks screen with the same secret as RAZORPAY_WEBHOOK_SECRET.")
    public ResponseEntity<MessageResponse> webhook(@RequestBody String rawPayload,
                                                     @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.handleWebhook(rawPayload, signature);
        return ResponseEntity.ok(new MessageResponse("ok"));
    }
}
