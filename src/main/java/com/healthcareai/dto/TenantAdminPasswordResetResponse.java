package com.healthcareai.dto;

/**
 * Returned exactly once, in the direct response of {@code POST
 * /api/super-admin/tenants/{id}/reset-admin-password}. {@code
 * temporaryPassword} is never persisted anywhere in plaintext (only its
 * bcrypt hash is saved) and is not emailed - the super admin is expected to
 * copy and relay it to the clinic out-of-band. This is the account-recovery
 * path that works even when SMTP/email isn't configured (see
 * PasswordResetServiceImpl for the email-based self-service alternative).
 */
public record TenantAdminPasswordResetResponse(
        String adminEmail,
        String temporaryPassword
) {
}
