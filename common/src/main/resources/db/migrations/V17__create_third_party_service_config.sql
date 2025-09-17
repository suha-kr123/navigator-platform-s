-- Create f_third_party_service_config table for integration service configurations
CREATE TABLE f_third_party_service_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Service identification
    name VARCHAR NOT NULL,              -- Human-readable name of the service
    service VARCHAR NOT NULL,           -- Service type identifier (e.g., 'otp', 'sms', 'voice')
    
    -- Configuration references
    primary_config_key UUID NOT NULL,   -- Reference to the primary provider configuration
    fallback_config_key UUID,           -- Reference to the fallback provider configuration
    
    -- Service behavior
    retry_count INTEGER NOT NULL DEFAULT 0,    -- Number of retry attempts before fallback
    is_primary BOOLEAN NOT NULL DEFAULT true,  -- Indicates if this is the primary service configuration
    is_active BOOLEAN NOT NULL DEFAULT true,   -- Indicates if the service is currently active
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR,
    version INTEGER NOT NULL DEFAULT 0
);


