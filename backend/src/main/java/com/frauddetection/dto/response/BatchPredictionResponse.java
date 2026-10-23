package com.frauddetection.dto.response;

import java.util.List;

public class BatchPredictionResponse {
    private int totalProcessed;
    private int fraudDetected;
    private int legitimateDetected;
    private double fraudPercentage;
    private List<PredictionResponse> results;

    public BatchPredictionResponse() {}

    public BatchPredictionResponse(int totalProcessed, int fraudDetected, int legitimateDetected, List<PredictionResponse> results) {
        this.totalProcessed = totalProcessed;
        this.fraudDetected = fraudDetected;
        this.legitimateDetected = legitimateDetected;
        this.fraudPercentage = totalProcessed > 0 ? ((double) fraudDetected / totalProcessed) * 100.0 : 0.0;
        this.results = results;
    }

    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }

    public int getFraudDetected() { return fraudDetected; }
    public void setFraudDetected(int fraudDetected) { this.fraudDetected = fraudDetected; }

    public int getLegitimateDetected() { return legitimateDetected; }
    public void setLegitimateDetected(int legitimateDetected) { this.legitimateDetected = legitimateDetected; }

    public double getFraudPercentage() { return fraudPercentage; }
    public void setFraudPercentage(double fraudPercentage) { this.fraudPercentage = fraudPercentage; }

    public List<PredictionResponse> getResults() { return results; }
    public void setResults(List<PredictionResponse> results) { this.results = results; }
}
