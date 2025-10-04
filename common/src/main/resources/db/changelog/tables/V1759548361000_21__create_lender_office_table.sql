CREATE TABLE lender_office
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name       varchar(100) NOT NULL,
    key        varchar(100) NOT NULL,
    lender_key varchar(20)  NOT NULL,
    address_id uuid         NOT NULL,
    status     varchar(40)  NOT NULL,
    created_by varchar(255),
    created_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    version    bigint           DEFAULT 0,
    CONSTRAINT uq_lender_office_key UNIQUE (key),
    CONSTRAINT fk_lender_office_lender FOREIGN KEY (lender_key) REFERENCES lender (key)
);
