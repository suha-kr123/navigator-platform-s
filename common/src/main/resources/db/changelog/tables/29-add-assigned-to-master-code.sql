INSERT INTO master_code (id, key, name, description, is_system_defined, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440008', 'ASSIGNED_TO', '{"default": "Assigned To"}',
        '{"default": "Person assigned to handle the task"}', true, 'system', '2025-01-27 10:00:00', 0);

INSERT INTO master_code_value (id, key, code_key, value, description, is_active, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440801', 'JOHN_SMITH', 'ASSIGNED_TO', '{"default": "John Smith"}',
        '{"default": "Senior Loan Officer"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440802', 'SARAH_JOHNSON', 'ASSIGNED_TO', '{"default": "Sarah Johnson"}',
        '{"default": "Credit Analyst"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440803', 'MICHAEL_BROWN', 'ASSIGNED_TO', '{"default": "Michael Brown"}',
        '{"default": "Relationship Manager"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440804', 'EMMA_DAVIS', 'ASSIGNED_TO', '{"default": "Emma Davis"}',
        '{"default": "Documentation Specialist"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440805', 'DAVID_WILSON', 'ASSIGNED_TO', '{"default": "David Wilson"}',
        '{"default": "Risk Assessment Officer"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440806', 'LISA_ANDERSON', 'ASSIGNED_TO', '{"default": "Lisa Anderson"}',
        '{"default": "Customer Service Representative"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440807', 'ROBERT_TAYLOR', 'ASSIGNED_TO', '{"default": "Robert Taylor"}',
        '{"default": "Underwriting Manager"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440808', 'JENNIFER_MARTINEZ', 'ASSIGNED_TO', '{"default": "Jennifer Martinez"}',
        '{"default": "Compliance Officer"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440809', 'JAMES_GARCIA', 'ASSIGNED_TO', '{"default": "James Garcia"}',
        '{"default": "Branch Manager"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440810', 'AMANDA_LEE', 'ASSIGNED_TO', '{"default": "Amanda Lee"}',
        '{"default": "Operations Coordinator"}', true, 'system', '2025-01-27 10:00:00', 0);
