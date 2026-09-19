package com.healthcareai.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Issues, rotates, and revokes the opaque refresh tokens that let a staff
 * session silently obtain a new JWT access token without a full re-login
 * (see {@code AuthController} / {@code RefreshTokenServiceImpl}).
 */
public interface RefreshTokenService {

    record IssuedToken(String rawToken, Instant expiresAt) {
    }

    record RotationResult(UUID userId, IssuedToken newToken) {
    }

    /** Issues and persists (hashed) a brand-new refresh token for a user,
     * e.g. right after login. */
    IssuedToken issue(UUID userId);

    /**
     * Validates a raw refresh token and, if it is unused/unrevoked and not
     * expired, atomically revokes it and issues a replacement (rotation:
     * every refresh token is single-use, so a leaked-and-reused token is
     * immediately detectable/invalidated). Throws {@code
     * BusinessRuleViolationException} if the token is invalid, expired, or
     * already used.
     */
    RotationResult rotate(String rawToken);

    /** Revokes a single refresh token, e.g. on sign-out. Silently no-ops if
     * the token doesn't exist or is already revoked. */
    void revoke(String rawToken);

    /** Revokes every refresh token for a user - called whenever their
     * password changes (self-service, forgot-password, or an admin
     * resetting it), or when an admin deactivates their account, so no
     * stale session can keep renewing itself. */
    void revokeAllForUser(UUID userId);
}
