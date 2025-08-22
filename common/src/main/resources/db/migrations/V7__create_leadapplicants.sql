CREATE TABLE lead_applicant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL,
    person_id UUID NOT NULL,
    applicant_type VARCHAR(20) NOT NULL,
    relationship_to_primary VARCHAR(30) NOT NULL DEFAULT 'SELF',
    status VARCHAR(20) NOT NULL DEFAULT 'NEEDS_TO_BE_REVIEWED',
    data_ext JSONB,

    -- Audit columns from AuditableEntity
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version int NOT NULL DEFAULT '0'
);

CREATE INDEX idx_lead_applicant_lead_id ON lead_applicant (lead_id);
CREATE INDEX idx_lead_applicant_person_id ON lead_applicant (person_id);