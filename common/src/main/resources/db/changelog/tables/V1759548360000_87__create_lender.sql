CREATE TABLE lender
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    key        varchar(20)  NOT NULL,
    name       varchar(100) NOT NULL,
    status     varchar(40)  NOT NULL,
    created_by varchar(255),
    created_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    version    bigint           DEFAULT 0,
    CONSTRAINT uq_lender_key UNIQUE (key)
);
