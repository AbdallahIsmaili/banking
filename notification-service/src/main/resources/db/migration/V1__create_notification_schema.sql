
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(255),
    recipient VARCHAR(255),
    subject VARCHAR(500),
    message TEXT,
    type ENUM('EMAIL', 'SMS', 'IN_APP') NOT NULL,
    status ENUM('PENDING', 'SENT', 'READ', 'FAILED') NOT NULL,
    sent_at TIMESTAMP NULL,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index pour optimiser les requêtes fréquentes
CREATE INDEX idx_notification_client_id ON notification(client_id);
CREATE INDEX idx_notification_status ON notification(status);
CREATE INDEX idx_notification_type ON notification(type);
CREATE INDEX idx_notification_sent_at ON notification(sent_at);