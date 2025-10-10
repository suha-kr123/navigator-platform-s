CREATE TABLE person
(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name     varchar(100),
    middle_name    varchar(100),
    last_name      varchar(100),
    mobile_numbers jsonb,
    email          varchar(100),
    date_of_birth  date,
    gender         varchar(10),
    data_ext       jsonb,
    created_by     varchar(255),
    created_at     timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_by     varchar(255),
    updated_at     timestamp DEFAULT CURRENT_TIMESTAMP,
    version        bigint DEFAULT 0
);
