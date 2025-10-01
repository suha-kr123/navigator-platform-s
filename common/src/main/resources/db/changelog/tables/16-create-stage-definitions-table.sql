CREATE TABLE stage_definitions
(
    id                uuid PRIMARY KEY,
    key               varchar(255) NOT NULL UNIQUE,
    name              varchar(255) NOT NULL,
    description       text,
    pipeline_key      varchar(255) NOT NULL,
    possible_outcomes jsonb,
    created_by        varchar(255),
    created_at        timestamp,
    updated_by        varchar(255),
    updated_at        timestamp,
    version           bigint
);

INSERT INTO stage_definitions (id, key, name, description, pipeline_key, possible_outcomes, created_by, created_at,
                               updated_by, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440010', 'APPLICATION_RECEIVED', 'Application Received',
        'Initial stage when home loan application is submitted', 'HOME_LOAN', '[
    "DOCUMENT_COLLECTION",
    "REJECTED"
  ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440011', 'DOCUMENT_COLLECTION', 'Document Collection',
        'Collecting and verifying required documents for home loan', 'HOME_LOAN', '[
         "DOCUMENT_VERIFICATION",
         "REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440012', 'DOCUMENT_VERIFICATION', 'Document Verification',
        'Verifying authenticity and completeness of submitted documents', 'HOME_LOAN', '[
         "CREDIT_ASSESSMENT",
         "REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440013', 'CREDIT_ASSESSMENT', 'Credit Assessment',
        'Evaluating creditworthiness and financial capacity of the applicant', 'HOME_LOAN', '[
         "PROPERTY_VALUATION",
         "REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440014', 'PROPERTY_VALUATION', 'Property Valuation',
        'Conducting property valuation to determine loan-to-value ratio', 'HOME_LOAN', '[
         "UNDERWRITING",
         "REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440015', 'UNDERWRITING', 'Underwriting',
        'Final risk assessment and loan approval decision', 'HOME_LOAN', '[
         "APPROVED",
         "REJECTED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440016', 'APPROVED', 'Approved',
        'Loan application has been approved and ready for disbursement', 'HOME_LOAN', '[
         "DISBURSED"
       ]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440017', 'DISBURSED', 'Disbursed',
        'Loan amount has been disbursed to the borrower', 'HOME_LOAN', '[]', 'system', '2025-09-25 11:18:21.011', '',
        '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440018', 'REJECTED', 'Rejected', 'Loan application has been rejected',
        'HOME_LOAN', '[]', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0);
