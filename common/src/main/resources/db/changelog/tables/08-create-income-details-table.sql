CREATE TABLE income_details
(
    id                  uuid PRIMARY KEY,
    employment_type     varchar(255) NOT NULL,
    employer_name       varchar(255),
    employer_type       varchar(255),
    job_title           varchar(255),
    department          varchar(255),
    location            varchar(255),
    salary              numeric,
    verification_status varchar(255),
    verification_notes  varchar(255),
    data_ext            jsonb,
    created_by          varchar(255),
    created_at          timestamp,
    updated_by          varchar(255),
    updated_at          timestamp,
    version             bigint
);
