INSERT INTO code_master (id, code_name, code_value, created_by, created_at, updated_by, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440200', 'GENDER_OPTIONS',
        '{"MALE": {"name": "Male", "description": "Male gender", "active": true}, "FEMALE": {"name": "Female", "description": "Female gender", "active": true}, "OTHER": {"name": "Other", "description": "Other gender", "active": true}}',
        'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440201', 'APPLICANT_TYPES',
        '{"PRIMARY": {"name": "Primary Applicant", "description": "Primary loan applicant", "active": true}, "CO_APPLICANT": {"name": "Co-Applicant", "description": "Co-applicant for the loan", "active": true}, "GUARANTOR": {"name": "Guarantor", "description": "Loan guarantor", "active": true}}',
        'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440202', 'RELATIONSHIP_TYPES',
        '{"SELF": {"name": "Self", "description": "Self relationship", "active": true}, "SPOUSE": {"name": "Spouse", "description": "Spouse relationship", "active": true}, "FATHER": {"name": "Father", "description": "Father relationship", "active": true}, "MOTHER": {"name": "Mother", "description": "Mother relationship", "active": true}, "SON": {"name": "Son", "description": "Son relationship", "active": true}, "DAUGHTER": {"name": "Daughter", "description": "Daughter relationship", "active": true}, "BROTHER": {"name": "Brother", "description": "Brother relationship", "active": true}, "SISTER": {"name": "Sister", "description": "Sister relationship", "active": true}, "OTHER": {"name": "Other", "description": "Other relationship", "active": true}}',
        'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440203', 'VERIFICATION_STATUSES',
        '{"PENDING": {"name": "Pending", "description": "Verification pending", "active": true}, "VERIFIED": {"name": "Verified", "description": "Successfully verified", "active": true}, "REJECTED": {"name": "Rejected", "description": "Verification rejected", "active": true}, "IN_PROGRESS": {"name": "In Progress", "description": "Verification in progress", "active": true}}',
        'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0);
