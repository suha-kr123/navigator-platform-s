CREATE TABLE payments
(
    id             uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    payment_type   varchar(255) NOT NULL,
    payment_status varchar(50)  NOT NULL DEFAULT 'PENDING_PAYMENT',
    amount_paid    numeric,
    paid_at        timestamp,
    payment_method varchar(255),
    transaction_id varchar(255),
    remarks        text,
    data_ext       jsonb,
    created_by     varchar(255),
    created_at     timestamp             DEFAULT CURRENT_TIMESTAMP,
    updated_by     varchar(255),
    updated_at     timestamp             DEFAULT CURRENT_TIMESTAMP,
    version        bigint                DEFAULT 0
);
