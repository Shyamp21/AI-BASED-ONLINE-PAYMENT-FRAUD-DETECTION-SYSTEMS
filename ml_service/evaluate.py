"""
Model Evaluation and Benchmarking Script.
Generates comprehensive comparative performance metrics for:
- CatBoost Classifier
- XGBoost Classifier
- Soft Voting Ensemble
"""

import json
import os
import sys

def display_evaluation_report(metrics_path=None):
    if metrics_path is None:
        base_dir = os.path.dirname(os.path.abspath(__file__))
        metrics_path = os.path.join(base_dir, "models", "model_metrics.json")

    if not os.path.exists(metrics_path):
        print(f"Metrics file not found at {metrics_path}. Please run train_models.py first.")
        sys.exit(1)

    with open(metrics_path, "r", encoding="utf-8") as f:
        metrics = json.load(f)

    print("=" * 80)
    print("AI-BASED ONLINE PAYMENT FRAUD DETECTION - MODEL BENCHMARK REPORT")
    print(f"Dataset:       {metrics.get('dataset', 'PaySim Mobile Money')}")
    print(f"Model Version: {metrics.get('model_version', 'v1.0-ensemble')}")
    print("=" * 80)

    cb = metrics["catboost"]
    xb = metrics["xgboost"]
    ens = metrics["ensemble"]

    header = f"{'Metric':<22} | {'CatBoost':<14} | {'XGBoost':<14} | {'Voting Ensemble':<16}"
    sep = "-" * len(header)
    print(header)
    print(sep)

    for metric_key, label in [
        ("accuracy", "Accuracy"),
        ("precision", "Precision"),
        ("recall", "Recall"),
        ("f1_score", "F1-Score"),
        ("roc_auc", "ROC-AUC"),
        ("pr_auc", "PR-AUC (Avg Prec)"),
    ]:
        val_cb = f"{cb.get(metric_key, 0.0):.4f}"
        val_xb = f"{xb.get(metric_key, 0.0):.4f}"
        val_ens = f"{ens.get(metric_key, 0.0):.4f}"
        print(f"{label:<22} | {val_cb:<14} | {val_xb:<14} | {val_ens:<16}")

    print(sep)

    cm = ens["confusion_matrix"]
    print("\nENSEMBLE CONFUSION MATRIX (Isolated Test Set):")
    print(f"  True Positives  (Fraud correctly flagged):   {cm['true_positive']}")
    print(f"  False Positives (Legitimate flagged fraud):  {cm['false_positive']}")
    print(f"  True Negatives  (Legitimate allowed):        {cm['true_negative']}")
    print(f"  False Negatives (Fraud missed):              {cm['false_negative']}")
    print(f"  Sample Size Evaluated:                       {ens['sample_size']}")

    print("\nTOP PREDICTIVE FEATURES (Feature Importance Ranking):")
    for rank, feat in enumerate(metrics.get("feature_importances", []), start=1):
        bar = "=" * int(feat["importance"] * 40)
        print(f"  {rank}. {feat['feature']:<30} {feat['importance']:.4f}  [{bar}]")

    print("=" * 80)

if __name__ == "__main__":
    display_evaluation_report()
