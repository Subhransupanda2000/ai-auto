package com.healthcareai.service;

import java.util.List;
import java.util.Map;

import com.healthcareai.entity.AuditLog;

public interface AuditLogService {

    AuditLog record(String actor, String action, String entityType, String entityId,
                     Map<String, Object> details, String ipAddress);

    List<AuditLog> findForEntity(String entityType, String entityId);

    List<AuditLog> findByActor(String actor);
}
