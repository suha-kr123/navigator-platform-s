CREATE TABLE advisor
(
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id                uuid        NOT NULL,
    advisor_code             varchar(255),
    is_employee              boolean          DEFAULT false,
    status                   varchar(50) NOT NULL,
    is_experienced_dsa       boolean          DEFAULT false,
    remarks                  varchar(1000),
    rejection_reason         varchar(1000),
    advisor_feedback         varchar(2000),
    welcome_kit_sent         boolean          DEFAULT false,
    attended_advisor_meeting boolean          DEFAULT false,
    data_ext                 jsonb,
    created_by               varchar(255),
    created_at               timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by               varchar(255),
    updated_at               timestamp        DEFAULT CURRENT_TIMESTAMP,
    version                  bigint           DEFAULT 0
);
