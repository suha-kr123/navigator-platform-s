-- Create third_party_provider_config table for provider-specific configurations
CREATE TABLE third_party_provider_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Provider identification
    name VARCHAR NOT NULL,              -- Human-readable name of the provider
    provider VARCHAR NOT NULL,          -- Provider identifier (e.g., 'msg91', 'twilio', 'exotel')
    
    -- Configuration
    configs TEXT NOT NULL,              -- JSON string containing provider-specific configuration
    active BOOLEAN NOT NULL DEFAULT true, -- Indicates if the provider is currently active
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR,
    version INTEGER NOT NULL DEFAULT 0
);




