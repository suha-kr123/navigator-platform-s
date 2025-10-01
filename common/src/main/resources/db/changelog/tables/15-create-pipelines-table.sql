CREATE TABLE pipelines
(
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(255) NOT NULL,
    key         varchar(255) NOT NULL UNIQUE,
    description text,
    created_by  varchar(255),
    created_at  timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by  varchar(255),
    updated_at  timestamp        DEFAULT CURRENT_TIMESTAMP,
    version     bigint           DEFAULT 0
);

INSERT INTO pipelines (id, name, key, description, created_by, created_at, updated_by, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'Home Loan Pipeline', 'HOME_LOAN',
        'Complete workflow for home loan applications', 'system', '2025-09-25 11:18:21.011', '',
        '2025-09-25 11:18:21.011', 0);
