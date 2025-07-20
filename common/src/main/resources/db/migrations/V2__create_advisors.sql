CREATE TABLE advisor
(
    id           UUID PRIMARY KEY,
    person_id    UUID UNIQUE        NOT NULL,
    advisor_code VARCHAR(50) UNIQUE NOT NULL,
    status       VARCHAR(30)        NOT NULL,
    data_ext     JSONB,
    created_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_at   TIMESTAMP,
    updated_by   VARCHAR(100),
    version      int                NOT NULL DEFAULT '0'
);

CREATE INDEX idx_advisor_status ON advisor (status);