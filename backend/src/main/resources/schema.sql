-- Spring Boot Classpath Schema
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_transaction_id VARCHAR(64) NOT NULL UNIQUE,
    transaction_amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    step INT NOT NULL DEFAULT 1,
    sender_id VARCHAR(64) NOT NULL,
    old_balance_orig DECIMAL(15, 2) NOT NULL,
    new_balance_orig DECIMAL(15, 2) NOT NULL,
    receiver_id VARCHAR(64) NOT NULL,
    old_balance_dest DECIMAL(15, 2) NOT NULL,
    new_balance_dest DECIMAL(15, 2) NOT NULL,
    error_balance_orig DECIMAL(15, 2) DEFAULT 0.00,
    error_balance_dest DECIMAL(15, 2) DEFAULT 0.00,
    transaction_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prediction_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    predicted_class VARCHAR(20) NOT NULL,
    fraud_probability DECIMAL(6, 4) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    catboost_probability DECIMAL(6, 4),
    xgboost_probability DECIMAL(6, 4),
    ensemble_score DECIMAL(6, 4),
    prediction_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    explanation_summary TEXT
);

CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prediction_id BIGINT NOT NULL,
    review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    remarks TEXT,
    priority VARCHAR(20) DEFAULT 'HIGH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(64),
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
