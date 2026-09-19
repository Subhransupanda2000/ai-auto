package com.healthcareai.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcareai.entity.MessageChannel;
import com.healthcareai.entity.TenantMessageLog;

public interface TenantMessageLogRepository extends JpaRepository<TenantMessageLog, UUID> {

    /** Backs the "this month" / "last month" breakdown in
     * {@code TenantMessageLogService.getStats} - {@code [from, to)}. */
    long countByTenantIdAndChannelAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            UUID tenantId, MessageChannel channel, Instant from, Instant to);
}
