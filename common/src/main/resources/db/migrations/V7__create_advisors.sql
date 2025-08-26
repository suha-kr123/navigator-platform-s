CREATE TABLE advisor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id UUID NOT NULL,
    advisor_code VARCHAR(100) UNIQUE NOT NULL,
    is_employee BOOLEAN DEFAULT FALSE,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    is_experienced_dsa BOOLEAN DEFAULT FALSE,
    remarks VARCHAR(1000),
    rejection_reason VARCHAR(1000),
    advisor_feedback VARCHAR(2000),
    welcome_kit_sent BOOLEAN DEFAULT FALSE,
    attended_advisor_meeting BOOLEAN DEFAULT FALSE,
    data_ext JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_advisor_status ON advisor(status);
CREATE INDEX idx_advisor_person_id ON advisor(person_id);
CREATE INDEX idx_advisor_code ON advisor(advisor_code);
