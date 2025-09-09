CREATE TABLE task_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    identifier VARCHAR(100) NOT NULL,
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    actions_group VARCHAR(100) NOT NULL,
    condition_on_action JSONB,
    tat_hours JSONB,
    assignment_strategy VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    possible_statuses JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_task_definitions_key ON task_definitions(key);
CREATE INDEX idx_task_definitions_identifier ON task_definitions(identifier);
CREATE INDEX idx_task_definitions_actions_group ON task_definitions(actions_group);
CREATE INDEX idx_task_definitions_assignment_strategy ON task_definitions(assignment_strategy);
CREATE INDEX idx_task_definitions_priority ON task_definitions(priority);
