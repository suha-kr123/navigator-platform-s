CREATE TABLE documents
(
    id   uuid PRIMARY KEY default gen_random_uuid(),
    name varchar(255) NOT NULL,
    type varchar(255) NOT NULL,
    size bigint       NOT NULL,
    path text         NOT NULL,
    tags                jsonb,
    data_ext            jsonb,
    created_by          varchar(255),
    created_at          timestamp,
    updated_by          varchar(255),
    updated_at          timestamp,
    version             bigint
);
