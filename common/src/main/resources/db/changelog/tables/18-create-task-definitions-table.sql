CREATE TABLE task_definitions
(
    id                uuid PRIMARY KEY,
    name              varchar(255) NOT NULL,
    key               varchar(255) NOT NULL UNIQUE,
    type              varchar(255) NOT NULL,
    description       text,
    possible_outcomes jsonb,
    created_by        varchar(255),
    created_at        timestamp,
    updated_by        varchar(255),
    updated_at        timestamp,
    version           bigint
);

INSERT INTO task_definitions (id, name, key, type, description, possible_outcomes, created_by, created_at, updated_by,
                              updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440020', 'Review Application', 'REVIEW_APPLICATION', 'REVIEW',
        'Review submitted home loan application for completeness and initial eligibility', '[
    "APPROVED",
    "REJECTED",
    "REQUEST_MORE_INFO"
  ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440021', 'Collect Documents', 'COLLECT_DOCUMENTS', 'COLLECTION',
        'Collect all required documents from the applicant', '[
         "COMPLETED",
         "INCOMPLETE"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440022', 'Verify Documents', 'VERIFY_DOCUMENTS', 'VERIFICATION',
        'Verify authenticity and validity of submitted documents', '[
         "VERIFIED",
         "REJECTED",
         "REQUEST_RESUBMISSION"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440023', 'Credit Check', 'CREDIT_CHECK', 'ASSESSMENT',
        'Perform credit bureau check and assess creditworthiness', '[
         "PASSED",
         "FAILED",
         "REQUIRES_MANUAL_REVIEW"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440024', 'Verify Income', 'VERIFY_INCOME', 'VERIFICATION',
        'Verify applicant''s income sources and stability', '[
         "VERIFIED",
         "INSUFFICIENT",
         "REQUIRES_ADDITIONAL_PROOF"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440025', 'Property Valuation', 'PROPERTY_VALUATION', 'VALUATION',
        'Conduct property valuation to determine market value and loan-to-value ratio', '[
         "COMPLETED",
         "REQUIRES_RE_VALUATION",
         "PROPERTY_REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440026', 'Legal Verification', 'LEGAL_VERIFICATION', 'VERIFICATION',
        'Verify legal title and property ownership documents', '[
         "CLEAR",
         "ENCUMBRANCE_FOUND",
         "TITLE_DEFECT"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440027', 'Underwriting Review', 'UNDERWRITING_REVIEW', 'REVIEW',
        'Final underwriting review and loan approval decision', '[
         "APPROVED",
         "REJECTED",
         "CONDITIONAL_APPROVAL"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440028', 'Loan Sanction', 'LOAN_SANCTION', 'PROCESSING',
        'Generate loan sanction letter and prepare for disbursement', '[
         "SANCTIONED",
         "REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440029', 'Loan Disbursement', 'LOAN_DISBURSEMENT', 'PROCESSING',
        'Process loan disbursement to borrower''s account', '[
         "DISBURSED",
         "FAILED",
         "PENDING"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440030', 'Customer Communication', 'CUSTOMER_COMMUNICATION', 'COMMUNICATION',
        'Communicate with customer about application status and requirements', '[
         "COMPLETED",
         "PENDING_RESPONSE",
         "ESCALATED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0);
