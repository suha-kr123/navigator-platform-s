CREATE TABLE pipelines
(
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(255) NOT NULL,
    key         varchar(255) NOT NULL UNIQUE,
    description text,
    created_by  varchar(255),
    created_at  timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by  varchar(255),
    updated_at  timestamp        DEFAULT CURRENT_TIMESTAMP,
    version     bigint           DEFAULT 0
);

