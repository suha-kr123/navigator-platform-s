CREATE TABLE advisor
(
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id                uuid        NOT NULL,
    advisor_code             varchar(255),
    status                   varchar(50) NOT NULL,
    rejection_reason_key varchar(50),
    data_ext                 jsonb,
    created_by               varchar(255),
    created_at               timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by               varchar(255),
    updated_at               timestamp        DEFAULT CURRENT_TIMESTAMP,
    version                  bigint           DEFAULT 0
);
