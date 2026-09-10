package com.healthcareai.dto;

import com.healthcareai.entity.Channel;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/chat}.
 */
public record ChatRequest(
        @NotBlank String sessionId,
        @NotBlank String message,
        Channel channel,
        String phoneNumber,

        /** Which clinic this message belongs to. Required for anonymous/
         * unauthenticated callers (e.g. a future WhatsApp/SMS webhook);
         * ignored for authenticated staff requests, which are always
         * scoped to the caller's own tenant instead. */
        String tenantSlug
) {
}
