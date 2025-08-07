CREATE TABLE api_audit_log (
    id UUID PRIMARY KEY,
    username VARCHAR(50),
    method VARCHAR(10) NOT NULL,
    uri TEXT NOT NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    request_body TEXT,
    response_body TEXT,
    response_status INT NOT NULL,
    error_message VARCHAR(100),
    duration_ms BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
