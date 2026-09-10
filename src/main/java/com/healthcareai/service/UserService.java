package com.healthcareai.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.healthcareai.entity.Role;
import com.healthcareai.entity.User;

public interface UserService {

    User createUser(String email, String rawPassword, String fullName, Role role, UUID tenantId);

    Optional<User> findByEmail(String email);

    /** Unconditionally sets a new password, used once a password-reset
     * token has already been validated. */
    void setPassword(UUID userId, String newRawPassword);

    /** Verifies {@code currentPassword} before setting {@code newPassword};
     * used for the authenticated self-service "change password" flow. */
    void changePassword(String email, String currentPassword, String newPassword);

    List<User> findByTenant(UUID tenantId);

    long count();

    long countByTenant(UUID tenantId);
}
