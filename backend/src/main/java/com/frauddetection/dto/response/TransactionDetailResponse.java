package com.frauddetection.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDetailResponse {
    private Long id;
    private String externalTransactionId;
    private BigDecimal amount;
    private String type;
    private Integer step;
    private String senderId;
    private BigDecimal oldBalanceOrig;
    private BigDecimal newBalanceOrig;
    private String receiverId;
    private BigDecimal oldBalanceDest;
    private BigDecimal newBalanceDest;
    private BigDecimal errorBalanceOrig;
    private BigDecimal errorBalanceDest;
    private LocalDateTime timestamp;
    private String predictedClass;
    private BigDecimal fraudProbability;
    private String riskLevel;
    private String explanationSummary;

    public TransactionDetailResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getStep() { return step; }
    public void setStep(Integer step) { this.step = step; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public BigDecimal getOldBalanceOrig() { return oldBalanceOrig; }
    public void setOldBalanceOrig(BigDecimal oldBalanceOrig) { this.oldBalanceOrig = oldBalanceOrig; }

    public BigDecimal getNewBalanceOrig() { return newBalanceOrig; }
    public void setNewBalanceOrig(BigDecimal newBalanceOrig) { this.newBalanceOrig = newBalanceOrig; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public BigDecimal getOldBalanceDest() { return oldBalanceDest; }
    public void setOldBalanceDest(BigDecimal oldBalanceDest) { this.oldBalanceDest = oldBalanceDest; }

    public BigDecimal getNewBalanceDest() { return newBalanceDest; }
    public void setNewBalanceDest(BigDecimal newBalanceDest) { this.newBalanceDest = newBalanceDest; }

    public BigDecimal getErrorBalanceOrig() { return errorBalanceOrig; }
    public void setErrorBalanceOrig(BigDecimal errorBalanceOrig) { this.errorBalanceOrig = errorBalanceOrig; }

    public BigDecimal getErrorBalanceDest() { return errorBalanceDest; }
    public void setErrorBalanceDest(BigDecimal errorBalanceDest) { this.errorBalanceDest = errorBalanceDest; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPredictedClass() { return predictedClass; }
    public void setPredictedClass(String predictedClass) { this.predictedClass = predictedClass; }

    public BigDecimal getFraudProbability() { return fraudProbability; }
    public void setFraudProbability(BigDecimal fraudProbability) { this.fraudProbability = fraudProbability; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getExplanationSummary() { return explanationSummary; }
    public void setExplanationSummary(String explanationSummary) { this.explanationSummary = explanationSummary; }
}
