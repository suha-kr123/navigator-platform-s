CREATE TABLE address
(
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    address_type   varchar(50) NOT NULL,
    address_one    text,
    address_two    text,
    landmark       text,
    district       varchar(255),
    state          varchar(255),
    pincode        varchar(20) NOT NULL,
    address_source varchar(50),
    data_ext       jsonb,
    created_by     varchar(255),
    created_at     timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by     varchar(255),
    updated_at     timestamp        DEFAULT CURRENT_TIMESTAMP,
    version        bigint           DEFAULT 0
);
