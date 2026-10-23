package com.frauddetection.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_transaction_id", nullable = false, unique = true, length = 64)
    private String externalTransactionId;

    @Column(name = "transaction_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal transactionAmount;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "step", nullable = false)
    private Integer step = 1;

    @Column(name = "sender_id", nullable = false, length = 64)
    private String senderId;

    @Column(name = "old_balance_orig", nullable = false, precision = 15, scale = 2)
    private BigDecimal oldBalanceOrig;

    @Column(name = "new_balance_orig", nullable = false, precision = 15, scale = 2)
    private BigDecimal newBalanceOrig;

    @Column(name = "receiver_id", nullable = false, length = 64)
    private String receiverId;

    @Column(name = "old_balance_dest", nullable = false, precision = 15, scale = 2)
    private BigDecimal oldBalanceDest;

    @Column(name = "new_balance_dest", nullable = false, precision = 15, scale = 2)
    private BigDecimal newBalanceDest;

    @Column(name = "error_balance_orig", precision = 15, scale = 2)
    private BigDecimal errorBalanceOrig = BigDecimal.ZERO;

    @Column(name = "error_balance_dest", precision = 15, scale = 2)
    private BigDecimal errorBalanceDest = BigDecimal.ZERO;

    @Column(name = "transaction_timestamp")
    private LocalDateTime transactionTimestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Transaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

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

    public LocalDateTime getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(LocalDateTime transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
