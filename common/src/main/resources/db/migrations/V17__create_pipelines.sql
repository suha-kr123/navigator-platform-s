CREATE TABLE pipelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_pipelines_key ON pipelines(key);
CREATE INDEX idx_pipelines_entity_type ON pipelines(entity_type);
