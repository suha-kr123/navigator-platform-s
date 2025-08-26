CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_status VARCHAR(100) NOT NULL DEFAULT 'PENDING_PAYMENT',
    amount_paid DECIMAL(19,2),
    paid_at TIMESTAMP,
    payment_method VARCHAR(100),
    transaction_id VARCHAR(255),
    remarks TEXT,
    data_ext JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payments_payment_status ON payments(payment_status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_paid_at ON payments(paid_at);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
