package com.healthcareai.dto;

/**
 * Returned exactly once by {@code POST /api/users/{id}/reset-password}
 * (an ADMIN resetting a staff member's password within their own clinic).
 * Never persisted or emailed - the admin is expected to relay it directly,
 * same pattern as {@link TenantAdminPasswordResetResponse}.
 */
public record UserPasswordResetResponse(
        String email,
        String temporaryPassword
) {
}
