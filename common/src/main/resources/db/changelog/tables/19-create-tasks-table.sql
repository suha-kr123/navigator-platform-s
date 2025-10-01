CREATE TABLE tasks
(
    id                  uuid PRIMARY KEY,
    task_definition_key varchar(255) NOT NULL,
    description         varchar(255),
    outcome             varchar(255) NOT NULL,
    status              varchar(255) NOT NULL,
    assigned_to         varchar(255),
    due_at              timestamp,
    completed_at        timestamp,
    rescheduled_at      timestamp,
    created_by          varchar(255),
    created_at          timestamp,
    updated_by          varchar(255),
    updated_at          timestamp,
    version             bigint
);
