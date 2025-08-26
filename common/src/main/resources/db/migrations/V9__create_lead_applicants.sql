CREATE TABLE lead_applicant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL,
    person_id UUID NOT NULL,
    applicant_type VARCHAR(100) NOT NULL,
    relationship_to_primary VARCHAR(100) NOT NULL DEFAULT 'SELF',
    status VARCHAR(100) NOT NULL DEFAULT 'NEEDS_TO_BE_REVIEWED',
    data_ext JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_lead_applicant_lead_id ON lead_applicant(lead_id);
CREATE INDEX idx_lead_applicant_person_id ON lead_applicant(person_id);
CREATE INDEX idx_lead_applicant_applicant_type ON lead_applicant(applicant_type);
CREATE INDEX idx_lead_applicant_status ON lead_applicant(status);
