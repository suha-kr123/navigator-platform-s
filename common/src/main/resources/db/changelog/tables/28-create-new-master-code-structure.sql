DROP TABLE IF EXISTS code_master;

CREATE TABLE master_code
(
    id                uuid PRIMARY KEY,
    parent_id         uuid,
    key               varchar(100) NOT NULL,
    name              jsonb,
    description       jsonb,
    is_system_defined boolean      NOT NULL DEFAULT false,
    created_by        varchar(255),
    created_at        timestamp,
    updated_by        varchar(255),
    updated_at        timestamp,
    version           bigint
);

CREATE TABLE master_code_value
(
    id          uuid PRIMARY KEY,
    key         varchar(100) NOT NULL,
    code_key    varchar(100) NOT NULL,
    value       jsonb,
    description jsonb,
    is_active   boolean      NOT NULL DEFAULT true,
    created_by  varchar(255),
    created_at  timestamp,
    updated_by  varchar(255),
    updated_at  timestamp,
    version     bigint
);

INSERT INTO master_code (id, key, name, description, is_system_defined, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'LOAN_PURPOSE', '{
  "default": "Loan Purpose"
}', '{
  "default": "Purpose for which the loan is taken"
}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440002', 'LOAN_PRODUCT', '{
         "default": "Loan Product"
       }', '{
         "default": "Type of loan product offered"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440003', 'SOURCING_CHANNEL', '{
         "default": "Sourcing Channel"
       }', '{
         "default": "Channel through which the lead was sourced"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440004', 'APPLICANT_TYPE', '{
         "default": "Applicant Type"
       }', '{
         "default": "Type of applicant in the loan application"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440005', 'RELATIONSHIP', '{
         "default": "Relationship"
       }', '{
         "default": "Relationship to primary applicant"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440006', 'GENDER', '{
         "default": "Gender"
       }', '{
         "default": "Gender of the person"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440007', 'TASK_TYPE', '{
         "default": "Task Type"
       }', '{
         "default": "Type of task to be performed"
       }', true, 'system', '2025-01-27 10:00:00', 0);

INSERT INTO master_code_value (id, key, code_key, value, description, is_active, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440101', 'HOUSE_CONSTRUCTION', 'LOAN_PURPOSE', '{
  "default": "House Construction"
}', '{
  "default": "Loan for house construction"
}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440102', 'BUSINESS_NEEDS', 'LOAN_PURPOSE', '{
         "default": "Business Needs"
       }', '{
         "default": "Loan for business requirements"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440103', 'BALANCE_TRANSFER', 'LOAN_PURPOSE', '{
         "default": "Balance Transfer"
       }', '{
         "default": "Loan for balance transfer from other lenders"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440201', 'HOME_LOAN', 'LOAN_PRODUCT', '{
         "default": "Home Loan"
       }', '{
         "default": "Loan for purchasing residential property"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440202', 'LAP', 'LOAN_PRODUCT', '{
         "default": "LAP"
       }', '{
         "default": "Loan Against Property"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440203', 'BT_LAP', 'LOAN_PRODUCT', '{
         "default": "BT - LAP"
       }', '{
         "default": "Balance Transfer - Loan Against Property"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440301', 'CONNECTOR', 'SOURCING_CHANNEL', '{
         "default": "Connector"
       }', '{
         "default": "Lead sourced through connector"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440302', 'NIVASA_EMPLOYEE', 'SOURCING_CHANNEL', '{
         "default": "Nivasa Employee"
       }', '{
         "default": "Lead sourced through Nivasa employee"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440303', 'DIRECT_WHATSAPP', 'SOURCING_CHANNEL', '{
         "default": "Direct - WhatsApp"
       }', '{
         "default": "Lead sourced directly through WhatsApp"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440304', 'DIRECT_CALL', 'SOURCING_CHANNEL', '{
         "default": "Direct - Call"
       }', '{
         "default": "Lead sourced directly through call"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440305', 'QR_CODE', 'SOURCING_CHANNEL', '{
         "default": "QR Code"
       }', '{
         "default": "Lead sourced through QR code"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440401', 'APPLICANT', 'APPLICANT_TYPE', '{
         "default": "Applicant"
       }', '{
         "default": "Primary applicant"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440402', 'CO_APPLICANT', 'APPLICANT_TYPE', '{
         "default": "Co- Applicant"
       }', '{
         "default": "Co-applicant in the loan"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440501', 'BROTHER', 'RELATIONSHIP', '{
         "default": "Brother"
       }', '{
         "default": "Brother relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440502', 'FATHER', 'RELATIONSHIP', '{
         "default": "Father"
       }', '{
         "default": "Father relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440503', 'MOTHER', 'RELATIONSHIP', '{
         "default": "Mother"
       }', '{
         "default": "Mother relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440504', 'SON', 'RELATIONSHIP', '{
         "default": "Son"
       }', '{
         "default": "Son relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440505', 'DAUGHTER', 'RELATIONSHIP', '{
         "default": "Daughter"
       }', '{
         "default": "Daughter relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440506', 'OTHERS', 'RELATIONSHIP', '{
         "default": "Others"
       }', '{
         "default": "Other relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440507', 'SISTER', 'RELATIONSHIP', '{
         "default": "Sister"
       }', '{
         "default": "Sister relationship"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440601', 'MALE', 'GENDER', '{
         "default": "Male"
       }', '{
         "default": "Male gender"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440602', 'FEMALE', 'GENDER', '{
         "default": "Female"
       }', '{
         "default": "Female gender"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440701', 'LOGIN_WITH_PARTNER', 'TASK_TYPE', '{
         "default": "Login With Partner"
       }', '{
         "default": "Task to login with partner system"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440702', 'DOCUMENT_COLLECTION', 'TASK_TYPE', '{
         "default": "Document Collection"
       }', '{
         "default": "Task to collect required documents"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440703', 'HOUSE_VISIT', 'TASK_TYPE', '{
         "default": "House Visit"
       }', '{
         "default": "Task to visit customer house"
       }', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440704', 'CUSTOMER_CALL', 'TASK_TYPE', '{
         "default": "Customer Call"
       }', '{
         "default": "Task to call customer"
       }', true, 'system', '2025-01-27 10:00:00', 0);
