-- Create third_party_response_log table for logging third-party API calls
CREATE TABLE third_party_response_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Entity context
    entity_type INTEGER NOT NULL,       -- Type of business entity making the request
    entity_id VARCHAR(20),              -- ID of the business entity
    
    -- Request details
    request_method VARCHAR(16) NOT NULL, -- HTTP method used (GET, POST, PUT, DELETE)
    url VARCHAR(512) NOT NULL,          -- Full URL of the third-party API endpoint
    request TEXT,                       -- Request payload sent to the third-party service
    
    -- Response details
    response TEXT,                      -- Response received from the third-party service
    http_status_code INTEGER,           -- HTTP status code returned by the third-party service
    response_time_ms BIGINT,            -- Response time in milliseconds
    
    -- Provider context
    provider_name VARCHAR(50),          -- Name of the provider used
    provider_config_id VARCHAR(50),     -- Reference to the provider configuration used
    
    -- Business context
    business_purpose TEXT,              -- Business purpose of the API call
    business_entity VARCHAR,            -- Name of the business entity
    api_purpose TEXT,                   -- Specific purpose of the API call
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR,
    version INTEGER NOT NULL DEFAULT 0
);




