CREATE TABLE stages
(
    id                   uuid PRIMARY KEY,
    stage_definition_key varchar(255) NOT NULL,
    outcome              varchar(255) NOT NULL,
    assigned_to          varchar(255),
    created_by           varchar(255),
    created_at           timestamp,
    updated_by           varchar(255),
    updated_at           timestamp,
    version              bigint
);
