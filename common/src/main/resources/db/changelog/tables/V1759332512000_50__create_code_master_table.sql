CREATE TABLE master_code
(
    id uuid PRIMARY KEY default gen_random_uuid(),
    parent_id         uuid,
    key               varchar(100) NOT NULL,
    name              jsonb,
    description       jsonb,
    is_system_defined boolean      NOT NULL DEFAULT false,
    created_by        varchar(255),
    created_at        timestamp,
    updated_by        varchar(255),
    updated_at        timestamp,
    version           bigint
);

CREATE TABLE master_code_value
(
    id uuid PRIMARY KEY default gen_random_uuid(),
    key         varchar(100) NOT NULL,
    code_key    varchar(100) NOT NULL,
    value       jsonb,
    description jsonb,
    is_active   boolean      NOT NULL DEFAULT true,
    created_by  varchar(255),
    created_at  timestamp,
    updated_by  varchar(255),
    updated_at  timestamp,
    version     bigint
);