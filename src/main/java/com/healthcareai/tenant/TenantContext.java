package com.healthcareai.tenant;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Thread-local holder for the current request's tenant id. Populated by the
 * JWT authentication filter (from the {@code tenantId} claim) for every
 * authenticated staff request, and read by {@link TenantIdentifierResolver}
 * so that Hibernate's {@code @TenantId} partitioned multitenancy can
 * transparently scope every query/insert against tenant-owned entities
 * (see {@code com.healthcareai.entity.Doctor}, {@code Patient},
 * {@code Appointment}, {@code Conversation}, {@code FaqDocument}) to the
 * caller's clinic.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getCurrentTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Runs {@code action} with the current thread's tenant context
     * temporarily set to {@code tenantId}, restoring whatever was there
     * beforehand once it completes. Used by super-admin-driven flows (e.g.
     * provisioning a brand-new tenant's first admin user) that must act on
     * behalf of a tenant without the calling principal itself belonging to
     * one.
     */
    public static <T> T callAs(UUID tenantId, Supplier<T> action) {
        UUID previous = CURRENT_TENANT.get();
        CURRENT_TENANT.set(tenantId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT_TENANT.remove();
            } else {
                CURRENT_TENANT.set(previous);
            }
        }
    }

    /** {@code void}-returning variant of {@link #callAs(UUID, Supplier)}. */
    public static void runAs(UUID tenantId, Runnable action) {
        callAs(tenantId, () -> {
            action.run();
            return null;
        });
    }
}
