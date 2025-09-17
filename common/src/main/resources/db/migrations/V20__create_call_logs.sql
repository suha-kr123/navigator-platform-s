-- Create call_logs table for Exotel voice integration
CREATE TABLE call_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_sid VARCHAR(50) UNIQUE NOT NULL,       	  	-- Exotel CallSid
    entity_name VARCHAR(50) NOT NULL,         	        -- e.g. 'Lead', 'Advisor', 'Applicant'
    entity_id BIGINT NOT NULL,                 				-- ID of that entity
    exophone VARCHAR(20),                       				-- Exotel virtual number used
    direction VARCHAR(20) NOT NULL,             			-- incoming | outgoing
    from_number VARCHAR(20) NOT NULL,
    to_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,                			-- queued, ringing, in-progress, completed, failed
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration INT,                               					-- in seconds
    recording_url TEXT,                        					-- Exotel link
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(50) DEFAULT 'system',
    updated_by VARCHAR(50) DEFAULT 'system',
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX idx_call_logs_status ON call_logs(status);


