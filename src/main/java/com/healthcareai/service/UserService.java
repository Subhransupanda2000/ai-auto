package com.healthcareai.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.dto.TenantAdminPasswordResetResponse;
import com.healthcareai.dto.UserPasswordResetResponse;
import com.healthcareai.entity.Role;
import com.healthcareai.entity.User;

public interface UserService {

    User createUser(String email, String rawPassword, String fullName, Role role, UUID tenantId);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID userId);

    /** Unconditionally sets a new password, used once a password-reset
     * token has already been validated. */
    void setPassword(UUID userId, String newRawPassword);

    /** Verifies {@code currentPassword} before setting {@code newPassword};
     * used for the authenticated self-service "change password" flow. */
    void changePassword(String email, String currentPassword, String newPassword);

    List<User> findByTenant(UUID tenantId);

    long count();

    long countByTenant(UUID tenantId);

    /** Super-admin-only account recovery: generates a new random temporary
     * password, sets it (bcrypt-hashed) on the tenant's admin account, and
     * returns it once so the caller can relay it to the clinic directly -
     * works even when SMTP/email isn't configured (unlike {@link
     * PasswordResetService}). */
    TenantAdminPasswordResetResponse resetTenantAdminPassword(UUID tenantId);

    /** Enables or disables a staff member within {@code tenantId} (an
     * ADMIN managing their own clinic's team). Refuses to let {@code
     * callerEmail} deactivate their own account, and refuses to leave a
     * tenant with zero active ADMINs. */
    User setStaffEnabled(UUID tenantId, UUID userId, boolean enabled, String callerEmail);

    /** Same account-recovery mechanism as {@link #resetTenantAdminPassword},
     * but for any staff member of {@code tenantId} (not just the admin),
     * used by that clinic's own ADMIN. */
    UserPasswordResetResponse resetStaffPassword(UUID tenantId, UUID userId);
}
