package com.frauddetection.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertResponse {
    private Long id;
    private Long predictionId;
    private Long transactionId;
    private String externalTransactionId;
    private BigDecimal amount;
    private String type;
    private BigDecimal fraudProbability;
    private String riskLevel;
    private String reviewStatus; // PENDING, REVIEWED, RESOLVED
    private String priority;
    private String reviewedByUsername;
    private LocalDateTime reviewedAt;
    private String remarks;
    private LocalDateTime createdAt;
    private String explanationSummary;

    public AlertResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPredictionId() { return predictionId; }
    public void setPredictionId(Long predictionId) { this.predictionId = predictionId; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getFraudProbability() { return fraudProbability; }
    public void setFraudProbability(BigDecimal fraudProbability) { this.fraudProbability = fraudProbability; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getReviewedByUsername() { return reviewedByUsername; }
    public void setReviewedByUsername(String reviewedByUsername) { this.reviewedByUsername = reviewedByUsername; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getExplanationSummary() { return explanationSummary; }
    public void setExplanationSummary(String explanationSummary) { this.explanationSummary = explanationSummary; }
}
