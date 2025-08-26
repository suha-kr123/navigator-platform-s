CREATE TABLE person_identifiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id UUID NOT NULL,
    identifier VARCHAR(100) NOT NULL,
    type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_person_identifiers_person_id ON person_identifiers(person_id);
CREATE INDEX idx_person_identifiers_type ON person_identifiers(type);
CREATE INDEX idx_person_identifiers_identifier ON person_identifiers(identifier);
CREATE UNIQUE INDEX idx_person_identifiers_unique ON person_identifiers(person_id, type, identifier);
