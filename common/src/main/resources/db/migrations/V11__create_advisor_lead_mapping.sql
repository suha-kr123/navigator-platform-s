CREATE TABLE advisor_lead_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    advisor_id UUID NOT NULL,
    lead_id UUID NOT NULL,
    payment_id UUID,
    remarks TEXT,
    data_ext JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_advisor_lead_mapping_unique ON advisor_lead_mapping(advisor_id, lead_id);
CREATE INDEX idx_advisor_lead_mapping_advisor_id ON advisor_lead_mapping(advisor_id);
CREATE INDEX idx_advisor_lead_mapping_lead_id ON advisor_lead_mapping(lead_id);
CREATE INDEX idx_advisor_lead_mapping_payment_id ON advisor_lead_mapping(payment_id);
