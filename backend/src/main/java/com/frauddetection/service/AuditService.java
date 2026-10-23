package com.frauddetection.service;

import com.frauddetection.entity.AuditLog;
import com.frauddetection.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(Long userId, String username, String action, String entityType, String entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog(userId, username, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }
}
