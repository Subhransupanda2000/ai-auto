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
        String phoneNumber
) {
}
