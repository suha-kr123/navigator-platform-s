CREATE TABLE address (
    id UUID PRIMARY KEY,
    address_one VARCHAR(50),
    address_two VARCHAR(50),
    landmark VARCHAR(50),
    district VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10) NOT NULL,
    address_source VARCHAR(30),
    data_ext JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version int NOT NULL DEFAULT '0'
);

CREATE INDEX idx_address_state ON address(state, district, pincode);
