CREATE TABLE stage_definitions
(
    id                uuid PRIMARY KEY,
    key               varchar(255) NOT NULL UNIQUE,
    name              varchar(255) NOT NULL,
    description       text,
    pipeline_key      varchar(255) NOT NULL,
    possible_outcomes jsonb,
    created_by        varchar(255),
    created_at        timestamp,
    updated_by        varchar(255),
    updated_at        timestamp,
    version           bigint
);
