CREATE TABLE lead_persons
(
    id                  uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    lead_id             uuid         NOT NULL,
    person_id           uuid         NOT NULL,
    relationship        varchar(50)  NOT NULL,
    is_primary          boolean      NOT NULL DEFAULT false,
    verification_status varchar(255) NOT NULL,
    verification_notes  text,
    ext_data            jsonb,
    created_by          varchar(255),
    created_at          timestamp             DEFAULT CURRENT_TIMESTAMP,
    updated_by          varchar(255),
    updated_at          timestamp             DEFAULT CURRENT_TIMESTAMP,
    version             bigint                DEFAULT 0
);
