package com.healthcareai.integration.razorpay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Currency;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcareai.config.RazorpayProperties;
import com.healthcareai.exception.PaymentGatewayException;
import com.healthcareai.integration.RazorpayClient;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link RazorpayClient} implementation backed by Razorpay's REST API
 * (Orders resource) plus manual HMAC-SHA256 signature verification, as
 * documented at https://razorpay.com/docs/payments/server-integration/ and
 * https://razorpay.com/docs/webhooks/validate-test/.
 */
@Component
@Slf4j
public class RazorpayClientImpl implements RazorpayClient {

    private final RestClient razorpayRestClient;
    private final RazorpayProperties properties;

    public RazorpayClientImpl(@Qualifier("razorpayRestClient") RestClient razorpayRestClient,
                               RazorpayProperties properties) {
        this.razorpayRestClient = razorpayRestClient;
        this.properties = properties;
    }

    @PostConstruct
    void logConfiguration() {
        boolean keyPresent = properties.keyId() != null && !properties.keyId().isBlank()
                && properties.keySecret() != null && !properties.keySecret().isBlank();
        boolean webhookSecretPresent = properties.webhookSecret() != null && !properties.webhookSecret().isBlank();
        log.info("Razorpay client configured (key-present={}, webhook-secret-present={}, base-url={})",
                keyPresent, webhookSecretPresent, properties.baseUrl());
        if (!keyPresent) {
            log.warn("No Razorpay API key configured (RAZORPAY_KEY_ID/RAZORPAY_KEY_SECRET are empty). "
                    + "Every checkout request will fail authentication until these are set.");
        }
    }

    @Override
    public RazorpayOrder createOrder(String receipt, BigDecimal amount, String currency) {
        long amountInMinorUnits = toMinorUnits(amount, currency);
        try {
            OrderResponse response = razorpayRestClient.post()
                    .uri("/orders")
                    .body(new OrderRequest(amountInMinorUnits, currency, receipt))
                    .retrieve()
                    .body(OrderResponse.class);
            if (response == null) {
                throw new PaymentGatewayException("Razorpay returned an empty order response.");
            }
            return new RazorpayOrder(response.id(), amountInMinorUnits, currency);
        } catch (RestClientException e) {
            log.error("Failed to create Razorpay order for receipt {}", receipt, e);
            throw new PaymentGatewayException("Failed to create Razorpay order. Check that "
                    + "RAZORPAY_KEY_ID/RAZORPAY_KEY_SECRET are configured correctly.", e);
        }
    }

    @Override
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        return hmacSha256Hex(payload, properties.keySecret()).equals(razorpaySignature);
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        return hmacSha256Hex(payload, properties.webhookSecret()).equals(signature);
    }

    @Override
    public String getPublicKeyId() {
        return properties.keyId();
    }

    /** Razorpay expects amounts in the currency's smallest unit (e.g.
     * paise for INR, cents for USD) - most currencies use 2 decimal
     * places, but not all (e.g. JPY has 0), so this defers to the JDK's
     * {@link Currency} metadata rather than hard-coding a x100 multiplier. */
    private long toMinorUnits(BigDecimal amount, String currencyCode) {
        int fractionDigits = Currency.getInstance(currencyCode).getDefaultFractionDigits();
        BigDecimal multiplier = BigDecimal.TEN.pow(Math.max(fractionDigits, 0));
        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to compute HMAC-SHA256 signature.", e);
        }
    }

    private record OrderRequest(long amount, String currency, String receipt) {
    }

    // Razorpay returns "notes" as either an empty array ([]) or an object
    // depending on whether any were set on the order - and it's unused
    // here anyway, so it's simply not mapped. @JsonIgnoreProperties guards
    // against any other fields Razorpay might add in the future.
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrderResponse(String id, long amount, String currency, String status,
                                  @JsonProperty("created_at") long createdAt) {
    }
}
