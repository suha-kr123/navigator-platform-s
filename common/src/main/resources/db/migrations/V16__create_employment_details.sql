-- Table: employment_details
CREATE TABLE employment_details (
    employment_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id          UUID NOT NULL,
    employer_name      TEXT,
    employer_type      VARCHAR(50),
    job_title          TEXT,
    department         TEXT,
    employment_type    VARCHAR(50),
    location           TEXT,
    salary             NUMERIC(12,2),
    documents          JSONB DEFAULT '{}'::jsonb,
    ext_data           JSONB DEFAULT '{}'::jsonb,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),
    version            BIGINT NOT NULL DEFAULT 0,
    UNIQUE(person_id)
);

-- Create audit triggers for employment_details table
CREATE TRIGGER tr_employment_details_audit_insert
    BEFORE INSERT ON employment_details
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_employment_details_audit_update
    BEFORE UPDATE ON employment_details
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create indexes
CREATE INDEX idx_employment_details_person_id ON employment_details(person_id);
CREATE INDEX idx_employment_details_employer_type ON employment_details(employer_type);
CREATE INDEX idx_employment_details_employment_type ON employment_details(employment_type);
CREATE INDEX idx_employment_details_documents ON employment_details USING GIN (documents);
CREATE INDEX idx_employment_details_ext_data ON employment_details USING GIN (ext_data);
