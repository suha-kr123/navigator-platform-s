CREATE TABLE tasks
(
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    task_definition_key varchar(255) NOT NULL,
    name                varchar(255) NOT NULL,
    description         varchar(255),
    outcome varchar(255),
    status              varchar(255) NOT NULL,
    assigned_to         varchar(255),
    due_at              timestamp,
    completed_at        timestamp,
    completed_by        varchar(255),
    created_by          varchar(255),
    created_at          timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_by          varchar(255),
    updated_at          timestamp DEFAULT CURRENT_TIMESTAMP,
    version             bigint DEFAULT 0
);
