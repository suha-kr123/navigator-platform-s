CREATE TABLE api_audit_log
(
    id              uuid PRIMARY KEY,
    duration_ms     bigint,
    error_message   varchar(255),
    ip_address      varchar(255),
    method          varchar(255),
    request_body    text,
    response_body   text,
    response_status integer,
    timestamp       timestamp,
    uri             varchar(255),
    user_agent      varchar(255),
    username        varchar(255)
);
