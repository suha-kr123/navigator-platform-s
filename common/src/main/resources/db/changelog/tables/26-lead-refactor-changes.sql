ALTER TABLE IF EXISTS address DROP COLUMN IF EXISTS is_primary;
ALTER TABLE IF EXISTS identifier DROP COLUMN IF EXISTS verification_status;
ALTER TABLE IF EXISTS identifier DROP COLUMN IF EXISTS verification_notes;
DROP TABLE IF EXISTS income_details;
ALTER TABLE IF EXISTS lead DROP COLUMN IF EXISTS income_detail_ids;
DROP TABLE IF EXISTS lead_persons;
ALTER TABLE IF EXISTS person DROP COLUMN IF EXISTS email;

INSERT INTO code_master (id, code_name, code_value, created_by, created_at, updated_by, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'IDENTIFIER_TYPE',
        '{"id": 1, "key": "AADHAR", "value": {"name": "Aadhar", "description": "Aadhar", "active": "true"}}', 'system',
        '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('550e8400-e29b-41d4-a716-446655440002', 'IDENTIFIER_TYPE',
        '{"id": 2, "key": "PAN", "value": {"name": "Pan", "description": "Pan", "active": "true"}}', 'system',
        '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('550e8400-e29b-41d4-a716-446655440003', 'IDENTIFIER_TYPE',
        '{"id": 3, "key": "VOTER_ID", "value": {"name": "Voter Id", "description": "Voter Id", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('550e8400-e29b-41d4-a716-446655440004', 'IDENTIFIER_TYPE',
        '{"id": 4, "key": "DRIVING_LICENSE", "value": {"name": "Driving License", "description": "Driving License", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('660e8400-e29b-41d4-a716-446655440001', 'LOAN_PURPOSE',
        '{"id": 1, "key": "HOME_PURCHASE", "value": {"name": "Home Purchase", "description": "Purchase of a new home", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('660e8400-e29b-41d4-a716-446655440002', 'LOAN_PURPOSE',
        '{"id": 2, "key": "HOME_CONSTRUCTION", "value": {"name": "Home Construction", "description": "Construction of a new home", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('660e8400-e29b-41d4-a716-446655440003', 'LOAN_PURPOSE',
        '{"id": 3, "key": "HOME_RENOVATION", "value": {"name": "Home Renovation", "description": "Renovation of existing home", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('660e8400-e29b-41d4-a716-446655440004', 'LOAN_PURPOSE',
        '{"id": 4, "key": "PLOT_PURCHASE", "value": {"name": "Plot Purchase", "description": "Purchase of a plot for construction", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0),
       ('660e8400-e29b-41d4-a716-446655440005', 'LOAN_PURPOSE',
        '{"id": 5, "key": "BALANCE_TRANSFER", "value": {"name": "Balance Transfer", "description": "Transfer of existing home loan", "active": "true"}}',
        'system', '2025-09-26 04:55:00.000', '', '2025-09-26 04:55:00.000', 0);
