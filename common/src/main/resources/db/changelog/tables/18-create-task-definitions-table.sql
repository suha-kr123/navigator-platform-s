CREATE TABLE task_definitions
(
    id                uuid PRIMARY KEY,
    name              varchar(255) NOT NULL,
    key               varchar(255) NOT NULL UNIQUE,
    type              varchar(255) NOT NULL,
    description       text,
    possible_outcomes jsonb,
    created_by        varchar(255),
    created_at        timestamp,
    updated_by        varchar(255),
    updated_at        timestamp,
    version           bigint
);
