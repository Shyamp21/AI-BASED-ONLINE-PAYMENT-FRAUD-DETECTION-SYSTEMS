package com.frauddetection.dto.response;

import java.util.List;
import java.util.Map;

public class DashboardStatisticsResponse {
    private Map<String, Long> transactionTypeDistribution;
    private Map<String, Long> riskLevelDistribution;
    private Map<String, Long> predictedClassDistribution;
    private List<Map<String, Object>> dailyTrends;
    private List<TransactionDetailResponse> recentActivity;

    public DashboardStatisticsResponse() {}

    public Map<String, Long> getTransactionTypeDistribution() { return transactionTypeDistribution; }
    public void setTransactionTypeDistribution(Map<String, Long> transactionTypeDistribution) { this.transactionTypeDistribution = transactionTypeDistribution; }

    public Map<String, Long> getRiskLevelDistribution() { return riskLevelDistribution; }
    public void setRiskLevelDistribution(Map<String, Long> riskLevelDistribution) { this.riskLevelDistribution = riskLevelDistribution; }

    public Map<String, Long> getPredictedClassDistribution() { return predictedClassDistribution; }
    public void setPredictedClassDistribution(Map<String, Long> predictedClassDistribution) { this.predictedClassDistribution = predictedClassDistribution; }

    public List<Map<String, Object>> getDailyTrends() { return dailyTrends; }
    public void setDailyTrends(List<Map<String, Object>> dailyTrends) { this.dailyTrends = dailyTrends; }

    public List<TransactionDetailResponse> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<TransactionDetailResponse> recentActivity) { this.recentActivity = recentActivity; }
}
