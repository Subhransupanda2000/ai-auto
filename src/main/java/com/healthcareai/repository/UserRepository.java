package com.healthcareai.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.Role;
import com.healthcareai.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Global (not tenant-scoped): email is unique across all tenants and
    // must be resolvable before the caller's tenant is known, i.e. at login.
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Tenant-scoped: for listing/managing staff within a single clinic.
    List<User> findByTenantId(UUID tenantId);

    long countByTenantId(UUID tenantId);

    /** The clinic's original/primary admin account (earliest-created ADMIN
     * for the tenant) - used by the super-admin "reset admin password"
     * recovery flow (see UserServiceImpl.resetTenantAdminPassword), which
     * doesn't depend on SMTP/email being configured at all. */
    Optional<User> findFirstByTenantIdAndRoleOrderByCreatedAtAsc(UUID tenantId, Role role);
}
