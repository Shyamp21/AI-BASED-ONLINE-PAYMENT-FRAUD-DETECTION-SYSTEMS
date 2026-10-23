-- Seed initial demo transactions and predictions for dashboard visualization
MERGE INTO users (id, username, email, password_hash, role, full_name, is_active)
KEY(id)
VALUES 
(1, 'admin', 'admin@fraudguard.ai', '$2a$10$GfO74H8s3B.a6xY7o8wFaeGq3R0pA5xR0QjL6XmJ6QzK9e4D7M4K2', 'ROLE_ADMIN', 'System Administrator', TRUE),
(2, 'analyst', 'analyst@fraudguard.ai', '$2a$10$GfO74H8s3B.a6xY7o8wFaeGq3R0pA5xR0QjL6XmJ6QzK9e4D7M4K2', 'ROLE_USER', 'Risk Analyst', TRUE);

MERGE INTO transactions (id, external_transaction_id, transaction_amount, transaction_type, step, sender_id, old_balance_orig, new_balance_orig, receiver_id, old_balance_dest, new_balance_dest, error_balance_orig, error_balance_dest, created_by)
KEY(id)
VALUES
(1, 'TXN-LEGIT-1001', 125.50, 'PAYMENT', 12, 'C1029384756', 1500.00, 1374.50, 'M9876543210', 0.00, 0.00, 0.00, 0.00, 1),
(2, 'TXN-FRAUD-2001', 98500.00, 'TRANSFER', 14, 'C8492031920', 98500.00, 0.00, 'C9918237192', 0.00, 0.00, 0.00, 98500.00, 1),
(3, 'TXN-FRAUD-2002', 98500.00, 'CASH_OUT', 15, 'C9918237192', 98500.00, 0.00, 'M4412998811', 1000.00, 99500.00, 0.00, 0.00, 1),
(4, 'TXN-LEGIT-1002', 320.00, 'CASH_IN', 20, 'C3829103948', 450.00, 770.00, 'C1029384756', 1374.50, 1374.50, 0.00, 0.00, 2),
(5, 'TXN-LEGIT-1003', 45.90, 'PAYMENT', 24, 'C7738291029', 2400.00, 2354.10, 'M1230984567', 0.00, 0.00, 0.00, 0.00, 2),
(6, 'TXN-FRAUD-2003', 450000.00, 'TRANSFER', 32, 'C5544332211', 450000.00, 0.00, 'C6655443322', 150.00, 150.00, 0.00, 450000.00, 1),
(7, 'TXN-LEGIT-1004', 1500.00, 'DEBIT', 40, 'C9988776655', 8500.00, 7000.00, 'C3344556677', 12000.00, 13500.00, 0.00, 0.00, 2);

MERGE INTO prediction_results (id, transaction_id, model_version, predicted_class, fraud_probability, risk_level, catboost_probability, xgboost_probability, ensemble_score, explanation_summary)
KEY(id)
VALUES
(1, 1, 'v1.0-ensemble', 'LEGITIMATE', 0.0210, 'LOW', 0.0190, 0.0230, 0.0210, 'Normal merchant payment behavior with consistent source balance deduction.'),
(2, 2, 'v1.0-ensemble', 'FRAUD', 0.9420, 'HIGH', 0.9380, 0.9460, 0.9420, 'High risk: Full account drain via TRANSFER to empty destination account.'),
(3, 3, 'v1.0-ensemble', 'FRAUD', 0.9150, 'HIGH', 0.9200, 0.9100, 0.9150, 'High risk: Immediate CASH_OUT matching earlier fraudulent TRANSFER balance.'),
(4, 4, 'v1.0-ensemble', 'LEGITIMATE', 0.0150, 'LOW', 0.0120, 0.0180, 0.0150, 'Standard cash deposit incrementing origin account.'),
(5, 5, 'v1.0-ensemble', 'LEGITIMATE', 0.0080, 'LOW', 0.0070, 0.0090, 0.0080, 'Low value retail transaction with verified merchant recipient.'),
(6, 6, 'v1.0-ensemble', 'FRAUD', 0.9780, 'HIGH', 0.9820, 0.9740, 0.9780, 'Critical alert: Large transfer exhausting balance without credit to recipient.'),
(7, 7, 'v1.0-ensemble', 'LEGITIMATE', 0.0450, 'LOW', 0.0400, 0.0500, 0.0450, 'Debit adjustment with proportional destination balance increase.');

MERGE INTO fraud_alerts (id, prediction_id, review_status, reviewed_by, remarks, priority)
KEY(id)
VALUES
(1, 2, 'REVIEWED', 1, 'Investigated by fraud team; confirmed automated bot sweep. Account flagged.', 'HIGH'),
(2, 3, 'RESOLVED', 1, 'Associated ATM cashout frozen; linked with Case #2001.', 'HIGH'),
(3, 6, 'PENDING', NULL, 'Awaiting compliance supervisor review; outbound wire hold recommended.', 'HIGH');

MERGE INTO audit_logs (id, user_id, username, action, entity_type, entity_id, details, ip_address)
KEY(id)
VALUES
(1, 1, 'admin', 'SYSTEM_INIT', 'SYSTEM', 'SYS-001', 'FraudGuard AI detection engine initialized with CatBoost + XGBoost Voting Ensemble v1.0', '127.0.0.1'),
(2, 1, 'admin', 'ALERT_REVIEW', 'FRAUD_ALERT', '2', 'Updated review status to REVIEWED for transaction TXN-FRAUD-2001', '192.168.1.100'),
(3, 1, 'admin', 'ALERT_RESOLVE', 'FRAUD_ALERT', '3', 'Updated review status to RESOLVED for transaction TXN-FRAUD-2002', '192.168.1.100');
