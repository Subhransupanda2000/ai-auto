package com.healthcareai.dto;

/**
 * Response body for {@code POST /api/chat}.
 */
public record ChatResponse(String sessionId, String reply) {
}
