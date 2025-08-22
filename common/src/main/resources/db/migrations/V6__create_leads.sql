CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requested_amount NUMERIC(19, 2) NULL,
    purpose VARCHAR(40) NULL,
    product_code VARCHAR(50) NULL,
    status VARCHAR(20) NULL,
    stage VARCHAR(30) NULL,
    preimerly_information JSONB NULL,
    lead_contacts JSONB NULL,
    sourcing_channel VARCHAR(50) NULL,

    -- Audit columns from AuditableEntity
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version int NOT NULL DEFAULT '0'
);

CREATE INDEX idx_leads_product_code ON leads (product_code);