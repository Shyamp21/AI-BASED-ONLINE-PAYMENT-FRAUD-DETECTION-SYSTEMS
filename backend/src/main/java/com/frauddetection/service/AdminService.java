package com.frauddetection.service;

import com.frauddetection.dto.response.ModelMetricsResponse;
import com.frauddetection.dto.response.UserResponse;
import com.frauddetection.entity.AuditLog;
import com.frauddetection.repository.AuditLogRepository;
import com.frauddetection.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final MlClientService mlClientService;

    public AdminService(
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            MlClientService mlClientService
    ) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.mlClientService = mlClientService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.getFullName(),
                        u.getIsActive(),
                        u.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public ModelMetricsResponse getModelMetrics() {
        return mlClientService.getModelMetrics();
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
