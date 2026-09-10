package com.healthcareai.service;

public interface PasswordResetService {

    /**
     * If {@code email} belongs to a staff account, emails it a single-use
     * password reset link. Always completes silently (no exception, no
     * indication either way) when the email doesn't match anyone, so this
     * endpoint can't be used to enumerate registered accounts.
     */
    void requestReset(String email);

    /**
     * Validates the token from a reset link and, if it is unused and not
     * expired, sets the associated account's password and consumes the
     * token.
     */
    void resetPassword(String token, String newPassword);
}
