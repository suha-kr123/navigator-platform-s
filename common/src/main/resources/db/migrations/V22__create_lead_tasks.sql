CREATE TABLE lead_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_key VARCHAR(100) NOT NULL,
    lead_id UUID NOT NULL,
    stage VARCHAR(100) NOT NULL,
    task_data JSONB,
    assigned_to VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP,
    completed_at TIMESTAMP,
    rescheduled_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_lead_tasks_task_key ON lead_tasks(task_key);
CREATE INDEX idx_lead_tasks_lead_id ON lead_tasks(lead_id);
CREATE INDEX idx_lead_tasks_stage ON lead_tasks(stage);
CREATE INDEX idx_lead_tasks_assigned_to ON lead_tasks(assigned_to);
CREATE INDEX idx_lead_tasks_status ON lead_tasks(status);
CREATE INDEX idx_lead_tasks_due_at ON lead_tasks(due_at);
