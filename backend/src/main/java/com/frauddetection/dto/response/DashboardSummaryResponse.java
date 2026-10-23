package com.frauddetection.dto.response;

public class DashboardSummaryResponse {
    private long totalTransactions;
    private long totalFraudDetected;
    private long totalLegitimate;
    private double fraudPercentage;
    private long pendingAlerts;
    private double totalAmountAnalyzed;

    public DashboardSummaryResponse() {}

    public DashboardSummaryResponse(long totalTransactions, long totalFraudDetected, long totalLegitimate, double fraudPercentage, long pendingAlerts, double totalAmountAnalyzed) {
        this.totalTransactions = totalTransactions;
        this.totalFraudDetected = totalFraudDetected;
        this.totalLegitimate = totalLegitimate;
        this.fraudPercentage = fraudPercentage;
        this.pendingAlerts = pendingAlerts;
        this.totalAmountAnalyzed = totalAmountAnalyzed;
    }

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public long getTotalFraudDetected() { return totalFraudDetected; }
    public void setTotalFraudDetected(long totalFraudDetected) { this.totalFraudDetected = totalFraudDetected; }

    public long getTotalLegitimate() { return totalLegitimate; }
    public void setTotalLegitimate(long totalLegitimate) { this.totalLegitimate = totalLegitimate; }

    public double getFraudPercentage() { return fraudPercentage; }
    public void setFraudPercentage(double fraudPercentage) { this.fraudPercentage = fraudPercentage; }

    public long getPendingAlerts() { return pendingAlerts; }
    public void setPendingAlerts(long pendingAlerts) { this.pendingAlerts = pendingAlerts; }

    public double getTotalAmountAnalyzed() { return totalAmountAnalyzed; }
    public void setTotalAmountAnalyzed(double totalAmountAnalyzed) { this.totalAmountAnalyzed = totalAmountAnalyzed; }
}
