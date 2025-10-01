CREATE TABLE master_pincode
(
    id            uuid PRIMARY KEY,
    pincode       varchar(10)  NOT NULL,
    area          varchar(255) NOT NULL,
    district      varchar(255),
    state         varchar(255),
    country       varchar(255),
    is_servicable boolean      NOT NULL,
    created_by    varchar(255),
    created_at    timestamp,
    updated_by    varchar(255),
    updated_at    timestamp,
    version       bigint
);
