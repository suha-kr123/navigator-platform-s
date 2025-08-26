CREATE TABLE api_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100),
    method VARCHAR(100) NOT NULL,
    uri TEXT NOT NULL,
    ip_address VARCHAR(100),
    user_agent TEXT,
    request_body TEXT,
    response_body TEXT,
    response_status INT NOT NULL,
    error_message TEXT,
    duration_ms BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_audit_log_timestamp ON api_audit_log(timestamp);
CREATE INDEX idx_api_audit_log_username ON api_audit_log(username);
CREATE INDEX idx_api_audit_log_method ON api_audit_log(method);
