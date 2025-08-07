CREATE TABLE IF NOT EXISTS master_pincode (
    id UUID PRIMARY KEY,
    pincode VARCHAR(10) NOT NULL,
    area VARCHAR(255) NOT NULL,
    district VARCHAR(255),
    country VARCHAR(255),
    is_servicable BOOLEAN,
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version int NOT NULL DEFAULT '0'
);

CREATE INDEX idx_master_pincode_pincode ON master_pincode(pincode);