CREATE TABLE identifiers
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier varchar(100) NOT NULL,
    type       varchar(100) NOT NULL,
    data_ext   jsonb,
    created_by varchar(255),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    version    bigint DEFAULT 0
);
