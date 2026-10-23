"""
Flask REST API Microservice for Fraud Detection Machine Learning Predictions.
Endpoints:
- GET  /health          : Health check and service readiness
- POST /predict         : Predict single transaction fraud probability
- POST /batch-predict   : Predict batch of transactions
- GET  /model-metrics   : Return model benchmark and evaluation statistics
"""

import json
import logging
import os
import sys
import time
from flask import Flask, request, jsonify

# Configure structured logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("fraud_ml_service")

# Initialize Flask
app = Flask(__name__)

# Import inference engine
try:
    from inference_engine import EnsembleInferenceEngine
    engine = EnsembleInferenceEngine()
    logger.info("Ensemble inference engine successfully initialized with model %s", engine.model_version)
except Exception as e:
    logger.error("Failed to initialize inference engine: %s", str(e), exc_info=True)
    engine = None

# Manual CORS header handler for all responses
@app.after_request
def add_cors_headers(response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Headers"] = "Content-Type,Authorization"
    response.headers["Access-Control-Allow-Methods"] = "GET,POST,OPTIONS"
    return response

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({
        "status": "UP",
        "service": "FraudGuard AI ML Engine",
        "model_version": engine.model_version if engine else "UNLOADED",
        "ready": engine is not None,
        "timestamp": int(time.time())
    }), 200

@app.route("/predict", methods=["POST", "OPTIONS"])
def predict():
    if request.method == "OPTIONS":
        return "", 200

    if engine is None:
        return jsonify({"error": "Model engine is not ready"}), 503

    payload = request.get_json(silent=True)
    if not payload:
        return jsonify({"error": "Invalid or missing JSON payload"}), 400

    # Validate minimal expected fields
    required = ["amount", "type"]
    missing = [f for f in required if f not in payload]
    if missing:
        return jsonify({"error": f"Missing required transaction fields: {missing}"}), 400

    try:
        start_time = time.time()
        result = engine.predict_one(payload)
        latency_ms = round((time.time() - start_time) * 1000, 2)
        result["latency_ms"] = latency_ms

        logger.info(
            "Prediction executed for type=%s amount=%.2f -> class=%s prob=%.4f (latency: %.2fms)",
            payload.get("type"), float(payload.get("amount", 0)),
            result["predicted_class"], result["fraud_probability"], latency_ms
        )
        return jsonify(result), 200
    except Exception as e:
        logger.error("Error during prediction inference: %s", str(e), exc_info=True)
        return jsonify({"error": f"Inference failed: {str(e)}"}), 500

@app.route("/batch-predict", methods=["POST", "OPTIONS"])
def batch_predict():
    if request.method == "OPTIONS":
        return "", 200

    if engine is None:
        return jsonify({"error": "Model engine is not ready"}), 503

    payload = request.get_json(silent=True)
    if not payload:
        return jsonify({"error": "Invalid or missing JSON body"}), 400

    # Payload may be a list directly or an object with 'transactions' list
    txns = payload if isinstance(payload, list) else payload.get("transactions", [])
    if not isinstance(txns, list):
        return jsonify({"error": "Expected a list of transaction objects"}), 400

    if len(txns) == 0:
        return jsonify({"total_processed": 0, "predictions": []}), 200

    if len(txns) > 10000:
        return jsonify({"error": "Batch size exceeds 10,000 limit"}), 413

    try:
        start_time = time.time()
        results = engine.predict_batch(txns)
        total_time_ms = round((time.time() - start_time) * 1000, 2)

        fraud_count = sum(1 for r in results if r["predicted_class"] == "FRAUD")
        legit_count = len(results) - fraud_count

        return jsonify({
            "total_processed": len(results),
            "fraud_detected": fraud_count,
            "legitimate_detected": legit_count,
            "total_latency_ms": total_time_ms,
            "predictions": results
        }), 200
    except Exception as e:
        logger.error("Error during batch inference: %s", str(e), exc_info=True)
        return jsonify({"error": f"Batch inference failed: {str(e)}"}), 500

@app.route("/model-metrics", methods=["GET"])
def model_metrics():
    metrics_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "models", "model_metrics.json")
    if not os.path.exists(metrics_path):
        return jsonify({"error": "Metrics file not found"}), 404

    with open(metrics_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return jsonify(data), 200

if __name__ == "__main__":
    port = int(os.environ.get("ML_PORT", 5001))
    print(f"Starting Fraud Detection ML Microservice on port {port}...")
    app.run(host="0.0.0.0", port=port, debug=False)
