package com.frauddetection.dto.response;

import java.util.Map;

public class ModelMetricsResponse {
    private String dataset;
    private String modelVersion;
    private Map<String, Object> catboost;
    private Map<String, Object> xgboost;
    private Map<String, Object> ensemble;
    private Object featureImportances;

    public ModelMetricsResponse() {}

    public String getDataset() { return dataset; }
    public void setDataset(String dataset) { this.dataset = dataset; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Map<String, Object> getCatboost() { return catboost; }
    public void setCatboost(Map<String, Object> catboost) { this.catboost = catboost; }

    public Map<String, Object> getXgboost() { return xgboost; }
    public void setXgboost(Map<String, Object> xgboost) { this.xgboost = xgboost; }

    public Map<String, Object> getEnsemble() { return ensemble; }
    public void setEnsemble(Map<String, Object> ensemble) { this.ensemble = ensemble; }

    public Object getFeatureImportances() { return featureImportances; }
    public void setFeatureImportances(Object featureImportances) { this.featureImportances = featureImportances; }
}
