CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requested_amount NUMERIC(19, 2),
    purpose VARCHAR(100),
    product_code VARCHAR(100),
    status VARCHAR(100) DEFAULT 'NEW',
    stage VARCHAR(100) DEFAULT 'INITIATED',
    preliminary_information JSONB,
    lead_contacts JSONB,
    sourcing_channel VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_leads_product_code ON leads(product_code);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_stage ON leads(stage);
CREATE INDEX idx_leads_sourcing_channel ON leads(sourcing_channel);
