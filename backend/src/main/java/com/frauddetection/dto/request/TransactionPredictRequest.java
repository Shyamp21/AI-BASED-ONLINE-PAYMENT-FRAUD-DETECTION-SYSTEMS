package com.frauddetection.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransactionPredictRequest {

    private String externalTransactionId;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required (PAYMENT, TRANSFER, CASH_OUT, CASH_IN, DEBIT)")
    private String type;

    private Integer step = 1;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotNull(message = "Original balance of sender is required")
    private BigDecimal oldbalanceOrg;

    @NotNull(message = "New balance of sender is required")
    private BigDecimal newbalanceOrig;

    @NotBlank(message = "Receiver ID is required")
    private String receiverId;

    @NotNull(message = "Original balance of receiver is required")
    private BigDecimal oldbalanceDest;

    @NotNull(message = "New balance of receiver is required")
    private BigDecimal newbalanceDest;

    public TransactionPredictRequest() {}

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

    public BigDecimal getOldbalanceOrg() { return oldbalanceOrg; }
    public void setOldbalanceOrg(BigDecimal oldbalanceOrg) { this.oldbalanceOrg = oldbalanceOrg; }

    public BigDecimal getNewbalanceOrig() { return newbalanceOrig; }
    public void setNewbalanceOrig(BigDecimal newbalanceOrig) { this.newbalanceOrig = newbalanceOrig; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public BigDecimal getOldbalanceDest() { return oldbalanceDest; }
    public void setOldbalanceDest(BigDecimal oldbalanceDest) { this.oldbalanceDest = oldbalanceDest; }

    public BigDecimal getNewbalanceDest() { return newbalanceDest; }
    public void setNewbalanceDest(BigDecimal newbalanceDest) { this.newbalanceDest = newbalanceDest; }
}
