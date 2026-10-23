-- =====================================================================
-- Seed Data for AI-Based Online Payment Fraud Detection System
-- Default Passwords:
-- admin / Admin@123  (BCrypt: $2a$10$dx.2Oq58xXw3JjB/9.L88.pUqB8aNl4o61oNkjfS0aL6h2iW19.eS or updated on boot)
-- user / User@123
-- =====================================================================

INSERT INTO users (username, email, password_hash, role, full_name, is_active)
VALUES 
('admin', 'admin@fraudguard.ai', '$2a$10$w6K0Kk8r89i7W4f7Fk4a6uB3a3wY8A9.xL6O5T7mB9r3aW1e2i3kO', 'ROLE_ADMIN', 'Security Administrator', TRUE),
('analyst', 'analyst@fraudguard.ai', '$2a$10$w6K0Kk8r89i7W4f7Fk4a6uB3a3wY8A9.xL6O5T7mB9r3aW1e2i3kO', 'ROLE_USER', 'Risk Analyst', TRUE)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

INSERT INTO transactions (external_transaction_id, transaction_amount, transaction_type, step, sender_id, old_balance_orig, new_balance_orig, receiver_id, old_balance_dest, new_balance_dest, error_balance_orig, error_balance_dest, transaction_timestamp, created_by)
VALUES
('TXN-LEGIT-1001', 125.50, 'PAYMENT', 12, 'C1029384756', 1500.00, 1374.50, 'M9876543210', 0.00, 0.00, 0.00, 0.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 1),
('TXN-FRAUD-2001', 98500.00, 'TRANSFER', 14, 'C8492031920', 98500.00, 0.00, 'C9918237192', 0.00, 0.00, 0.00, 98500.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), 1),
('TXN-FRAUD-2002', 98500.00, 'CASH_OUT', 15, 'C9918237192', 98500.00, 0.00, 'M4412998811', 1000.00, 99500.00, 0.00, 0.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), 1),
('TXN-LEGIT-1002', 320.00, 'CASH_IN', 20, 'C3829103948', 450.00, 770.00, 'C1029384756', 1374.50, 1374.50, 0.00, 0.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 2),
('TXN-LEGIT-1003', 45.90, 'PAYMENT', 24, 'C7738291029', 2400.00, 2354.10, 'M1230984567', 0.00, 0.00, 0.00, 0.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 2),
('TXN-FRAUD-2003', 450000.00, 'TRANSFER', 32, 'C5544332211', 450000.00, 0.00, 'C6655443322', 150.00, 150.00, 0.00, 450000.00, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 1),
('TXN-LEGIT-1004', 1500.00, 'DEBIT', 40, 'C9988776655', 8500.00, 7000.00, 'C3344556677', 12000.00, 13500.00, 0.00, 0.00, CURRENT_TIMESTAMP, 2);

INSERT INTO prediction_results (transaction_id, model_version, predicted_class, fraud_probability, risk_level, catboost_probability, xgboost_probability, ensemble_score, prediction_timestamp, explanation_summary)
VALUES
(1, 'v1.0-ensemble', 'LEGITIMATE', 0.0210, 'LOW', 0.0190, 0.0230, 0.0210, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 'Normal merchant payment behavior with consistent source balance deduction.'),
(2, 'v1.0-ensemble', 'FRAUD', 0.9420, 'HIGH', 0.9380, 0.9460, 0.9420, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), 'High risk: Full account drain via TRANSFER to empty destination account.'),
(3, 'v1.0-ensemble', 'FRAUD', 0.9150, 'HIGH', 0.9200, 0.9100, 0.9150, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), 'High risk: Immediate CASH_OUT matching earlier fraudulent TRANSFER balance.'),
(4, 'v1.0-ensemble', 'LEGITIMATE', 0.0150, 'LOW', 0.0120, 0.0180, 0.0150, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 'Standard cash deposit incrementing origin account.'),
(5, 'v1.0-ensemble', 'LEGITIMATE', 0.0080, 'LOW', 0.0070, 0.0090, 0.0080, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'Low value retail transaction with verified merchant recipient.'),
(6, 'v1.0-ensemble', 'FRAUD', 0.9780, 'HIGH', 0.9820, 0.9740, 0.9780, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'Critical alert: Large transfer exhausting balance without credit to recipient.'),
(7, 'v1.0-ensemble', 'LEGITIMATE', 0.0450, 'LOW', 0.0400, 0.0500, 0.0450, CURRENT_TIMESTAMP, 'Debit adjustment with proportional destination balance increase.');

INSERT INTO fraud_alerts (prediction_id, review_status, reviewed_by, reviewed_at, remarks, priority)
VALUES
(2, 'REVIEWED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 'Investigated by fraud team; confirmed automated bot sweep. Account flagged.', 'HIGH'),
(3, 'RESOLVED', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 'Associated ATM cashout frozen; linked with Case #2001.', 'HIGH'),
(6, 'PENDING', NULL, NULL, 'Awaiting compliance supervisor review; outbound wire hold recommended.', 'HIGH');

INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, ip_address, timestamp)
VALUES
(1, 'admin', 'SYSTEM_INIT', 'SYSTEM', 'SYS-001', 'FraudGuard AI detection engine initialized with CatBoost + XGBoost Voting Ensemble v1.0', '127.0.0.1', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
(1, 'admin', 'ALERT_REVIEW', 'FRAUD_ALERT', '2', 'Updated review status to REVIEWED for transaction TXN-FRAUD-2001', '192.168.1.100', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)),
(1, 'admin', 'ALERT_RESOLVE', 'FRAUD_ALERT', '3', 'Updated review status to RESOLVED for transaction TXN-FRAUD-2002', '192.168.1.100', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY));
