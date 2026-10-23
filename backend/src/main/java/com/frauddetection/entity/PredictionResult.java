package com.frauddetection.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_results")
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion = "v1.0-ensemble";

    @Column(name = "predicted_class", nullable = false, length = 20)
    private String predictedClass; // 'FRAUD' or 'LEGITIMATE'

    @Column(name = "fraud_probability", nullable = false, precision = 6, scale = 4)
    private BigDecimal fraudProbability;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel; // 'LOW', 'MEDIUM', 'HIGH'

    @Column(name = "catboost_probability", precision = 6, scale = 4)
    private BigDecimal catboostProbability;

    @Column(name = "xgboost_probability", precision = 6, scale = 4)
    private BigDecimal xgboostProbability;

    @Column(name = "ensemble_score", precision = 6, scale = 4)
    private BigDecimal ensembleScore;

    @Column(name = "prediction_timestamp")
    private LocalDateTime predictionTimestamp = LocalDateTime.now();

    @Column(name = "explanation_summary", columnDefinition = "TEXT")
    private String explanationSummary;

    public PredictionResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getPredictedClass() { return predictedClass; }
    public void setPredictedClass(String predictedClass) { this.predictedClass = predictedClass; }

    public BigDecimal getFraudProbability() { return fraudProbability; }
    public void setFraudProbability(BigDecimal fraudProbability) { this.fraudProbability = fraudProbability; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public BigDecimal getCatboostProbability() { return catboostProbability; }
    public void setCatboostProbability(BigDecimal catboostProbability) { this.catboostProbability = catboostProbability; }

    public BigDecimal getXgboostProbability() { return xgboostProbability; }
    public void setXgboostProbability(BigDecimal xgboostProbability) { this.xgboostProbability = xgboostProbability; }

    public BigDecimal getEnsembleScore() { return ensembleScore; }
    public void setEnsembleScore(BigDecimal ensembleScore) { this.ensembleScore = ensembleScore; }

    public LocalDateTime getPredictionTimestamp() { return predictionTimestamp; }
    public void setPredictionTimestamp(LocalDateTime predictionTimestamp) { this.predictionTimestamp = predictionTimestamp; }

    public String getExplanationSummary() { return explanationSummary; }
    public void setExplanationSummary(String explanationSummary) { this.explanationSummary = explanationSummary; }
}
