CREATE TABLE lead_lender
(
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id           uuid        NOT NULL,
    lender_key        varchar(20) NOT NULL,
    status            varchar(40) NOT NULL,
    lender_office_key varchar(20),
    rm_details        jsonb,
    login_id          varchar(100),
    reject_reason     varchar(40),
    created_by        varchar(255),
    created_at        timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by        varchar(255),
    updated_at        timestamp        DEFAULT CURRENT_TIMESTAMP,
    version           bigint           DEFAULT 0,
    CONSTRAINT fk_lead_lender_lead FOREIGN KEY (lead_id) REFERENCES leads (id)
);
