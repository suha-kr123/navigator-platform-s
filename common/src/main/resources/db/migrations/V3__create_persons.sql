CREATE TABLE person (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    mobile_numbers JSONB,
    email VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(100),
    data_ext JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_person_mobile_numbers ON person USING GIN (mobile_numbers);
CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_date_of_birth ON person(date_of_birth);
