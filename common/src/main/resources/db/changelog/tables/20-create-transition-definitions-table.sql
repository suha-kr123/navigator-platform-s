CREATE TABLE transition_definitions
(
    id                      uuid PRIMARY KEY,
    pipeline                varchar(255) NOT NULL,
    from_stage              varchar(255) NOT NULL,
    to_stage                varchar(255) NOT NULL,
    condition_on_transition jsonb,
    created_by              varchar(255),
    created_at              timestamp,
    updated_by              varchar(255),
    updated_at              timestamp,
    version                 bigint
);