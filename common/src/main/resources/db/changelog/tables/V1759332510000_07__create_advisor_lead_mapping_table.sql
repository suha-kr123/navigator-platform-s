CREATE TABLE advisor_lead_mapping
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    advisor_id uuid NOT NULL,
    lead_id    uuid NOT NULL,
    data_ext   jsonb,
    created_by varchar(255),
    created_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    version    bigint           DEFAULT 0
);
