CREATE TABLE master_pincode (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pincode VARCHAR(10) NOT NULL,
    area VARCHAR(255) NOT NULL,
    district VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255) DEFAULT 'India',
    is_servicable BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_master_pincode_pincode ON master_pincode(pincode);
CREATE INDEX idx_master_pincode_district ON master_pincode(district);
CREATE INDEX idx_master_pincode_state ON master_pincode(state);
CREATE INDEX idx_master_pincode_servicable ON master_pincode(is_servicable);
