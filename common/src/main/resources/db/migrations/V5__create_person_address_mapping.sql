CREATE TABLE person_address_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id UUID NOT NULL,
    address_id UUID NOT NULL,
    address_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_person_address_mapping_person_id ON person_address_mapping(person_id);
CREATE INDEX idx_person_address_mapping_address_id ON person_address_mapping(address_id);
CREATE INDEX idx_person_address_mapping_address_type ON person_address_mapping(address_type);
CREATE UNIQUE INDEX idx_person_address_mapping_unique ON person_address_mapping(person_id, address_id, address_type);
