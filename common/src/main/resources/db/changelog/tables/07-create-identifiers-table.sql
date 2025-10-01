CREATE TABLE identifiers
(
    id                  uuid PRIMARY KEY,
    identifier          varchar(100) NOT NULL,
    type                varchar(100) NOT NULL,
    verification_status varchar(255),
    verification_notes  varchar(255),
    data_ext            jsonb,
    created_by          varchar(255),
    created_at          timestamp,
    updated_by          varchar(255),
    updated_at          timestamp,
    version             bigint
);
