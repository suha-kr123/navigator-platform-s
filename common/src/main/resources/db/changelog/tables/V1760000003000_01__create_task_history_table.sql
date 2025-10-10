CREATE TABLE task_history
(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id uuid NOT NULL,
    event_type varchar(50) NOT NULL,
    old_value text,
    new_value text,
    changed_by varchar(255) NOT NULL,
    changed_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add index for better query performance
CREATE INDEX idx_task_history_task_id ON task_history(task_id);
CREATE INDEX idx_task_history_changed_at ON task_history(changed_at);
