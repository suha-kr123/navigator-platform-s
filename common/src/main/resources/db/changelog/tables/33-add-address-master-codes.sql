INSERT INTO master_code (id, key, name, description, is_system_defined, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440010', 'ADDRESS_TYPE', '{"default": "Address Type"}',
        '{"default": "Type of address (Home, Office, etc.)"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440011', 'ADDRESS_SOURCE', '{"default": "Address Source"}',
        '{"default": "Source of address information"}', true, 'system', '2025-01-27 10:00:00', 0);

INSERT INTO master_code_value (id, key, code_key, value, description, is_active, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440910', 'HOME', 'ADDRESS_TYPE', '{"default": "Home Address"}',
        '{"default": "Residential home address"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440911', 'OFFICE', 'ADDRESS_TYPE', '{"default": "Office Address"}',
        '{"default": "Business office address"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440912', 'CORRESPONDENCE', 'ADDRESS_TYPE',
        '{"default": "Correspondence Address"}', '{"default": "Address for official correspondence"}', true, 'system',
        '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440913', 'PERMANENT', 'ADDRESS_TYPE', '{"default": "Permanent Address"}',
        '{"default": "Permanent residential address"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440920', 'APPLICANT', 'ADDRESS_SOURCE', '{"default": "Applicant Provided"}',
        '{"default": "Address provided by the applicant"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440921', 'VERIFICATION', 'ADDRESS_SOURCE', '{"default": "Field Verification"}',
        '{"default": "Address verified through field visit"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440922', 'DOCUMENT', 'ADDRESS_SOURCE', '{"default": "Document Verification"}',
        '{"default": "Address extracted from submitted documents"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440923', 'THIRD_PARTY', 'ADDRESS_SOURCE', '{"default": "Third Party"}',
        '{"default": "Address obtained from third party sources"}', true, 'system', '2025-01-27 10:00:00', 0);
