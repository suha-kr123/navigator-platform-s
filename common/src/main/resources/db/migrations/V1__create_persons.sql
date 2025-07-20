CREATE TABLE person
(
    id            UUID PRIMARY KEY,
    first_name    VARCHAR(100),
    middle_name   VARCHAR(100),
    last_name     VARCHAR(100),
    mobile_number JSONB,
    email         VARCHAR(100),
    date_of_birth DATE,
    gender        VARCHAR(10),
    identifiers   JSONB,
    addresses     JSONB,
    data_ext      JSONB,
    created_at    TIMESTAMP,
    created_by    VARCHAR(100),
    updated_at    TIMESTAMP,
    updated_by VARCHAR(100),
    version    int NOT NULL DEFAULT '0'
);

CREATE INDEX idx_person_mobile_number ON person
    USING GIN (mobile_number);

CREATE INDEX idx_person_identifiers ON person
    USING GIN (identifiers);