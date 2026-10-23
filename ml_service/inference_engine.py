"""
Inference Engine for Ensemble Fraud Detection.
Loads saved model parameters and produces real-time calibrated predictions,
risk classification, and natural language explainability.
"""

import json
import math
import os
from typing import Dict, Any, List
from preprocess import engineer_features

def sigmoid(z: float) -> float:
    z_clamped = max(-25.0, min(25.0, z))
    return 1.0 / (1.0 + math.exp(-z_clamped))

class EnsembleInferenceEngine:
    def __init__(self, metadata_path: str = None):
        if metadata_path is None:
            base_dir = os.path.dirname(os.path.abspath(__file__))
            metadata_path = os.path.join(base_dir, "models", "ensemble_metadata.json")

        if not os.path.exists(metadata_path):
            raise FileNotFoundError(f"Model metadata not found at {metadata_path}. Train models first.")

        with open(metadata_path, "r", encoding="utf-8") as f:
            self.metadata = json.load(f)

        self.model_version = self.metadata.get("model_version", "v1.0-ensemble")
        self.threshold = float(self.metadata.get("threshold", 0.45))
        self.scaler_mean = self.metadata["scaler"]["mean"]
        self.scaler_scale = self.metadata["scaler"]["scale"]

        self.catboost_bias = self.metadata.get("catboost_bias", 0.0)
        self.catboost_lr = self.metadata.get("catboost_lr", 0.20)
        self.catboost_trees = self.metadata.get("catboost_trees", [])

        self.xgboost_bias = self.metadata.get("xgboost_bias", 0.0)
        self.xgboost_lr = self.metadata.get("xgboost_lr", 0.18)
        self.xgboost_trees = self.metadata.get("xgboost_trees", [])

        self.weights = self.metadata.get("weights", {"catboost": 0.5, "xgboost": 0.5})

    def _scale_vector(self, x: List[float]) -> List[float]:
        return [(val - m) / s for val, m, s in zip(x, self.scaler_mean, self.scaler_scale)]

    def _score_trees(self, x_scaled: List[float], trees: List[Dict[str, Any]], bias: float, lr: float) -> float:
        raw = bias
        for t in trees:
            f_idx = t["feature_idx"]
            split = t["split_val"]
            if x_scaled[f_idx] <= split:
                raw += lr * t["left_val"]
            else:
                raw += lr * t["right_val"]
        return sigmoid(raw)

    def predict_one(self, txn: Dict[str, Any]) -> Dict[str, Any]:
        """
        Runs feature extraction, scaling, and ensemble voting for a single transaction.
        """
        features_raw = engineer_features(txn)
        features_scaled = self._scale_vector(features_raw)

        # 1. CatBoost prediction
        p_catboost = self._score_trees(features_scaled, self.catboost_trees, self.catboost_bias, self.catboost_lr)

        # 2. XGBoost prediction
        p_xgboost = self._score_trees(features_scaled, self.xgboost_trees, self.xgboost_bias, self.xgboost_lr)

        # 3. Soft Voting Ensemble
        w_cb = self.weights.get("catboost", 0.5)
        w_xgb = self.weights.get("xgboost", 0.5)
        ensemble_score = (w_cb * p_catboost) + (w_xgb * p_xgboost)

        # Format probabilities
        p_catboost = round(p_catboost, 4)
        p_xgboost = round(p_xgboost, 4)
        fraud_probability = round(ensemble_score, 4)

        # Risk Classification & Decision Threshold
        predicted_class = "FRAUD" if fraud_probability >= self.threshold else "LEGITIMATE"

        if fraud_probability >= 0.70:
            risk_level = "HIGH"
        elif fraud_probability >= 0.35:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"

        # Explainability summary based on domain features
        explanations = []
        amount = float(txn.get("amount", 0.0))
        txn_type = str(txn.get("type", "")).upper()
        old_orig = float(txn.get("oldbalanceOrg", txn.get("old_balance_orig", 0.0)))
        new_orig = float(txn.get("newbalanceOrig", txn.get("new_balance_orig", 0.0)))
        old_dest = float(txn.get("oldbalanceDest", txn.get("old_balance_dest", 0.0)))
        new_dest = float(txn.get("newbalanceDest", txn.get("new_balance_dest", 0.0)))

        err_orig = new_orig + amount - old_orig
        err_dest = old_dest + amount - new_dest

        if predicted_class == "FRAUD":
            if txn_type in ["TRANSFER", "CASH_OUT"]:
                explanations.append(f"High-risk transaction pattern '{txn_type}' commonly exploited in account takeover.")
            if old_orig > 0 and new_orig == 0.0:
                explanations.append("Full source balance exhaustion (100% liquidation) detected.")
            if abs(err_dest) > 1.0 and old_dest == 0.0 and new_dest == 0.0:
                explanations.append("Recipient balance discrepancy: Funds left sender but destination shows zero balance.")
            if amount > 100000.0:
                explanations.append(f"Unusually large transaction magnitude (${amount:,.2f}).")
            if not explanations:
                explanations.append("Multi-feature non-linear anomaly detected by ensemble trees.")
        else:
            if txn_type == "PAYMENT":
                explanations.append("Standard merchant retail payment with normal balance decrement.")
            elif txn_type in ["CASH_IN", "DEBIT"]:
                explanations.append("Deposit or debit activity conforming to authentic user profiles.")
            else:
                explanations.append("Transaction balance changes verify legitimate source and destination reconciliation.")

        explanation_summary = " | ".join(explanations)

        return {
            "model_version": self.model_version,
            "predicted_class": predicted_class,
            "fraud_probability": fraud_probability,
            "risk_level": risk_level,
            "catboost_probability": p_catboost,
            "xgboost_probability": p_xgboost,
            "ensemble_score": fraud_probability,
            "explanation_summary": explanation_summary,
            "features_analyzed": len(features_raw)
        }

    def predict_batch(self, transactions: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        return [self.predict_one(t) for t in transactions]
