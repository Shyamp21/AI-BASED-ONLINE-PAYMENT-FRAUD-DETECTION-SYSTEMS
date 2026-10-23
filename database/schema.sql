-- =====================================================================
-- Database Schema for AI-Based Online Payment Fraud Detection System
-- Target: MySQL 8.x / Compatible with ANSI SQL & H2
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_txn_external_id (external_transaction_id),
    INDEX idx_txn_type (transaction_type),
    INDEX idx_txn_timestamp (transaction_timestamp),
    CONSTRAINT fk_txn_user FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prediction_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    predicted_class VARCHAR(20) NOT NULL, -- 'FRAUD' or 'LEGITIMATE'
    fraud_probability DECIMAL(6, 4) NOT NULL, -- 0.0000 to 1.0000
    risk_level VARCHAR(20) NOT NULL, -- 'LOW', 'MEDIUM', 'HIGH'
    catboost_probability DECIMAL(6, 4),
    xgboost_probability DECIMAL(6, 4),
    ensemble_score DECIMAL(6, 4),
    prediction_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    explanation_summary TEXT,
    INDEX idx_pred_class (predicted_class),
    INDEX idx_pred_risk (risk_level),
    INDEX idx_pred_timestamp (prediction_timestamp),
    CONSTRAINT fk_pred_txn FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prediction_id BIGINT NOT NULL,
    review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'REVIEWED', 'RESOLVED'
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    remarks TEXT,
    priority VARCHAR(20) DEFAULT 'HIGH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_alert_status (review_status),
    INDEX idx_alert_created (created_at),
    CONSTRAINT fk_alert_pred FOREIGN KEY (prediction_id) REFERENCES prediction_results (id) ON DELETE CASCADE,
    CONSTRAINT fk_alert_user FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(64),
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
