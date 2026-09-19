package com.healthcareai.service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.config.ClinicProperties;
import com.healthcareai.dto.MessageStatsRange;
import com.healthcareai.dto.TenantMessageStatsResponse;
import com.healthcareai.entity.MessageChannel;
import com.healthcareai.entity.Tenant;
import com.healthcareai.entity.TenantMessageLog;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.repository.TenantMessageLogRepository;
import com.healthcareai.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantMessageLogServiceImpl implements TenantMessageLogService {

    private final TenantMessageLogRepository messageLogRepository;
    private final TenantRepository tenantRepository;
    private final ClinicProperties clinicProperties;

    @Override
    @Transactional
    public void recordWhatsappMessage(UUID tenantId) {
        messageLogRepository.save(TenantMessageLog.builder().tenantId(tenantId).channel(MessageChannel.WHATSAPP).build());
        tenantRepository.incrementWhatsappMessageCount(tenantId);
    }

    @Override
    @Transactional
    public void recordAiChatMessage(UUID tenantId) {
        messageLogRepository.save(TenantMessageLog.builder().tenantId(tenantId).channel(MessageChannel.AI_CHAT).build());
        tenantRepository.incrementAiChatMessageCount(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantMessageStatsResponse getStats(UUID tenantId, MessageStatsRange range) {
        if (range == MessageStatsRange.ALL_TIME) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Tenant", tenantId));
            return new TenantMessageStatsResponse(range, tenant.getWhatsappMessageCount(),
                    tenant.getAiChatMessageCount(), tenant.getCreatedAt(), Instant.now());
        }

        ZoneId zone = resolveZone();
        YearMonth currentMonth = YearMonth.now(zone);
        YearMonth targetMonth = range == MessageStatsRange.LAST_MONTH ? currentMonth.minusMonths(1) : currentMonth;

        Instant from = targetMonth.atDay(1).atStartOfDay(zone).toInstant();
        Instant to = targetMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

        long whatsappCount = messageLogRepository.countByTenantIdAndChannelAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                tenantId, MessageChannel.WHATSAPP, from, to);
        long aiChatCount = messageLogRepository.countByTenantIdAndChannelAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                tenantId, MessageChannel.AI_CHAT, from, to);

        return new TenantMessageStatsResponse(range, whatsappCount, aiChatCount, from, to);
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(clinicProperties.timezone());
        } catch (Exception e) {
            log.warn("Invalid clinic timezone '{}', falling back to UTC.", clinicProperties.timezone());
            return ZoneId.of("UTC");
        }
    }
}
