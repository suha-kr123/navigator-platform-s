CREATE TABLE code_master
(
    id         uuid PRIMARY KEY,
    code_name  varchar(100) NOT NULL,
    code_value jsonb        NOT NULL,
    created_by varchar(255),
    created_at timestamp,
    updated_by varchar(255),
    updated_at timestamp,
    version    bigint
);

INSERT INTO code_master (id, code_name, code_value, created_by, created_at, updated_by, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440100', 'PRODUCT_CODES', '{
  "HOME_LOAN": {
    "name": "Home Loan",
    "description": "Loan for purchasing residential property",
    "active": true
  },
  "PERSONAL_LOAN": {
    "name": "Personal Loan",
    "description": "Unsecured loan for personal expenses",
    "active": true
  },
  "CAR_LOAN": {
    "name": "Car Loan",
    "description": "Loan for purchasing vehicles",
    "active": true
  },
  "EDUCATION_LOAN": {
    "name": "Education Loan",
    "description": "Loan for educational purposes",
    "active": true
  }
}', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0),
       ('550e8400-e29b-41d4-a716-446655440101', 'SOURCING_CHANNELS', '{
         "ONLINE": {
           "name": "Online",
           "description": "Lead generated through online channels",
           "active": true
         },
         "BANK_BRANCH": {
           "name": "Bank Branch",
           "description": "Lead generated through bank branches",
           "active": true
         },
         "AGENT": {
           "name": "Agent",
           "description": "Lead generated through agents",
           "active": true
         },
         "REFERRAL": {
           "name": "Referral",
           "description": "Lead generated through customer referrals",
           "active": true
         },
         "CALL_CENTER": {
           "name": "Call Center",
           "description": "Lead generated through call center",
           "active": true
         },
         "PARTNER": {
           "name": "Partner",
           "description": "Lead generated through business partners",
           "active": true
         }
       }', 'system', '2025-09-25 11:18:21.011', '', '2025-09-25 11:18:21.011', 0);
