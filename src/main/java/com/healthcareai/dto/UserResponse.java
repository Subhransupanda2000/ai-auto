package com.healthcareai.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String fullName, String role, boolean enabled, Instant createdAt) {
}
