CREATE TABLE stage_definitions
(
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    key               varchar(255) NOT NULL UNIQUE,
    name              varchar(255) NOT NULL,
    description       text,
    pipeline_key      varchar(255) NOT NULL,
    possible_outcomes jsonb,
    created_by        varchar(255),
    created_at        timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_by        varchar(255),
    updated_at        timestamp DEFAULT CURRENT_TIMESTAMP,
    version           bigint DEFAULT 0
);
