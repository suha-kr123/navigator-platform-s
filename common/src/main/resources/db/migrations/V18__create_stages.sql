CREATE TABLE stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    pipeline_key VARCHAR(100) NOT NULL,
    action_groups JSONB,
    default_tasks JSONB,
    tasks_allowed JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_stages_key ON stages(key);
CREATE INDEX idx_stages_pipeline_key ON stages(pipeline_key);
