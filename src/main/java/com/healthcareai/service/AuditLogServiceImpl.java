package com.healthcareai.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.entity.AuditLog;
import com.healthcareai.repository.AuditLogRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog record(String actor, String action, String entityType, String entityId,
                            Map<String, Object> details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .tenantId(TenantContext.getCurrentTenantId())
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findForEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByActor(String actor) {
        return auditLogRepository.findByActorOrderByCreatedAtDesc(actor);
    }
}
