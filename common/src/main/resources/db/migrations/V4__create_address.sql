CREATE TABLE address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    address_one VARCHAR(255),
    address_two VARCHAR(255),
    landmark VARCHAR(100),
    district VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10) NOT NULL,
    address_source VARCHAR(100),
    data_ext JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_address_state ON address(state, district, pincode);
CREATE INDEX idx_address_pincode ON address(pincode);
CREATE INDEX idx_address_district ON address(district);
