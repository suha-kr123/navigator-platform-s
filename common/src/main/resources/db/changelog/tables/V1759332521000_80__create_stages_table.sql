CREATE TABLE stages
(
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_definition_key varchar(255) NOT NULL,
    outcome              varchar(255) NOT NULL,
    assigned_to          varchar(255),
    created_by           varchar(255),
    created_at           timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_by           varchar(255),
    updated_at           timestamp DEFAULT CURRENT_TIMESTAMP,
    version              bigint DEFAULT 0
);
