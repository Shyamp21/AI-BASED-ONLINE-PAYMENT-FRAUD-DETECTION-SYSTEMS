package com.frauddetection.service;

import com.frauddetection.dto.request.AlertStatusUpdateRequest;
import com.frauddetection.dto.response.AlertResponse;
import com.frauddetection.entity.FraudAlert;
import com.frauddetection.entity.PredictionResult;
import com.frauddetection.entity.Transaction;
import com.frauddetection.entity.User;
import com.frauddetection.exception.ResourceNotFoundException;
import com.frauddetection.repository.FraudAlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AlertService {

    private final FraudAlertRepository fraudAlertRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public AlertService(
            FraudAlertRepository fraudAlertRepository,
            AuthService authService,
            AuditService auditService
    ) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlerts(String status, Pageable pageable) {
        String filterStatus = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;
        Page<FraudAlert> alerts = fraudAlertRepository.findAlertsWithDetails(filterStatus, pageable);

        return alerts.map(this::mapToAlertResponse);
    }

    @Transactional
    public AlertResponse updateAlertStatus(Long alertId, AlertStatusUpdateRequest request, String ipAddress) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found with ID: " + alertId));

        User currentUser = authService.getCurrentAuthenticatedUser();

        alert.setReviewStatus(request.getStatus().toUpperCase());
        alert.setReviewedBy(currentUser);
        alert.setReviewedAt(LocalDateTime.now());
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            alert.setRemarks(request.getRemarks().trim());
        }

        FraudAlert updated = fraudAlertRepository.save(alert);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getUsername() : "ADMIN",
                "UPDATE_ALERT_STATUS",
                "FRAUD_ALERT",
                String.valueOf(alertId),
                "Status set to " + request.getStatus() + " with remarks: " + request.getRemarks(),
                ipAddress
        );

        return mapToAlertResponse(updated);
    }

    private AlertResponse mapToAlertResponse(FraudAlert a) {
        AlertResponse res = new AlertResponse();
        res.setId(a.getId());
        res.setReviewStatus(a.getReviewStatus());
        res.setPriority(a.getPriority());
        res.setReviewedAt(a.getReviewedAt());
        res.setRemarks(a.getRemarks());
        res.setCreatedAt(a.getCreatedAt());

        if (a.getReviewedBy() != null) {
            res.setReviewedByUsername(a.getReviewedBy().getUsername());
        }

        PredictionResult pr = a.getPredictionResult();
        if (pr != null) {
            res.setPredictionId(pr.getId());
            res.setFraudProbability(pr.getFraudProbability());
            res.setRiskLevel(pr.getRiskLevel());
            res.setExplanationSummary(pr.getExplanationSummary());

            Transaction t = pr.getTransaction();
            if (t != null) {
                res.setTransactionId(t.getId());
                res.setExternalTransactionId(t.getExternalTransactionId());
                res.setAmount(t.getTransactionAmount());
                res.setType(t.getTransactionType());
            }
        }

        return res;
    }
}
