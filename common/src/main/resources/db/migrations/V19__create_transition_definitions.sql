CREATE TABLE transition_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline VARCHAR(100) NOT NULL,
    from_stage VARCHAR(100) NOT NULL,
    to_stage VARCHAR(100) NOT NULL,
    condition_on_transition JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_transition_definitions_pipeline ON transition_definitions(pipeline);
CREATE INDEX idx_transition_definitions_from_stage ON transition_definitions(from_stage);
CREATE INDEX idx_transition_definitions_to_stage ON transition_definitions(to_stage);
