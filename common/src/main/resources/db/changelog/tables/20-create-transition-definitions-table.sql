CREATE TABLE transition_definitions
(
    id                      uuid PRIMARY KEY,
    pipeline                varchar(255) NOT NULL,
    from_stage              varchar(255) NOT NULL,
    to_stage                varchar(255) NOT NULL,
    condition_on_transition jsonb,
    created_by              varchar(255),
    created_at              timestamp,
    updated_by              varchar(255),
    updated_at              timestamp,
    version                 bigint
);

INSERT INTO transition_definitions (id, pipeline, from_stage, to_stage, condition_on_transition, created_by, created_at,
                                    updated_by, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440040', 'HOME_LOAN', 'APPLICATION_RECEIVED', 'DOCUMENT_COLLECTION', '{
  "type": "automatic",
  "description": "Move to document collection after application review"
}', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440041', 'HOME_LOAN', 'APPLICATION_RECEIVED', 'REJECTED', '{
         "type": "conditional",
         "condition": "application_incomplete",
         "description": "Reject if application is incomplete or ineligible"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440042', 'HOME_LOAN', 'DOCUMENT_COLLECTION', 'DOCUMENT_VERIFICATION', '{
         "type": "automatic",
         "description": "Move to verification when all documents are collected"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440043', 'HOME_LOAN', 'DOCUMENT_COLLECTION', 'REJECTED', '{
         "type": "conditional",
         "condition": "documents_incomplete",
         "description": "Reject if required documents cannot be provided"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440044', 'HOME_LOAN', 'DOCUMENT_VERIFICATION', 'CREDIT_ASSESSMENT', '{
         "type": "automatic",
         "description": "Move to credit assessment when documents are verified"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440045', 'HOME_LOAN', 'DOCUMENT_VERIFICATION', 'REJECTED', '{
         "type": "conditional",
         "condition": "documents_invalid",
         "description": "Reject if documents are found to be invalid or fraudulent"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440046', 'HOME_LOAN', 'CREDIT_ASSESSMENT', 'PROPERTY_VALUATION', '{
         "type": "conditional",
         "condition": "credit_passed",
         "description": "Move to property valuation when credit assessment is positive"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440047', 'HOME_LOAN', 'CREDIT_ASSESSMENT', 'REJECTED', '{
         "type": "conditional",
         "condition": "credit_failed",
         "description": "Reject if credit assessment fails or debt-to-income ratio is too high"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440048', 'HOME_LOAN', 'PROPERTY_VALUATION', 'UNDERWRITING', '{
         "type": "conditional",
         "condition": "valuation_acceptable",
         "description": "Move to underwriting when property valuation meets loan-to-value requirements"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440049', 'HOME_LOAN', 'PROPERTY_VALUATION', 'REJECTED', '{
         "type": "conditional",
         "condition": "valuation_insufficient",
         "description": "Reject if property valuation is insufficient or property has issues"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440050', 'HOME_LOAN', 'UNDERWRITING', 'APPROVED', '{
         "type": "conditional",
         "condition": "underwriting_approved",
         "description": "Approve loan when underwriting review is positive"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440051', 'HOME_LOAN', 'UNDERWRITING', 'REJECTED', '{
         "type": "conditional",
         "condition": "underwriting_rejected",
         "description": "Reject loan when underwriting review determines high risk"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440052', 'HOME_LOAN', 'APPROVED', 'DISBURSED', '{
         "type": "automatic",
         "description": "Disburse loan amount after approval and completion of formalities"
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0);
