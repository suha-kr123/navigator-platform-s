-- =============================================================================
-- Navigator Platform – Full Schema DDL
-- Generated from JPA @Entity classes
--
-- IMPORTANT: Keep this file in sync with entity changes.
-- When you add/modify/remove a column, table, or JSONB structure in any
-- @Entity class, update the corresponding section in this file.
-- JSONB column structures are documented at the end of this file.
-- =============================================================================

-- =============================================================================
-- MODULE: person
-- =============================================================================

-- Entity: Person
CREATE TABLE n_person (
    id              BIGSERIAL       PRIMARY KEY,
    first_name      VARCHAR(100),
    middle_name     VARCHAR(100),
    last_name       VARCHAR(100),
    display_name    VARCHAR(300),
    email           VARCHAR(100),
    mobile_numbers  JSONB,
    address         JSONB,
    date_of_birth   DATE,
    gender          VARCHAR(10),
    identifiers     JSONB,
    data_ext        JSONB,
    cb_enquiry_id   JSONB,
    cb_details      JSONB,
    consent_details JSONB,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: usermanagement
-- =============================================================================

-- Entity: User
CREATE TABLE n_user (
    id          BIGSERIAL       PRIMARY KEY,
    person_id   BIGINT,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    status      VARCHAR(50),
    preferences JSONB,
    created_by  VARCHAR(255),
    created_at  TIMESTAMP,
    updated_by  VARCHAR(255),
    updated_at  TIMESTAMP,
    version     BIGINT          DEFAULT 0,
    CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES n_person (id)
);

-- =============================================================================
-- MODULE: rolemanagement
-- =============================================================================

-- Entity: Role
CREATE TABLE n_roles (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(255)    NOT NULL UNIQUE,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: Permission
CREATE TABLE n_permissions (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(255)    NOT NULL UNIQUE,
    action     VARCHAR(255),
    operation  VARCHAR(255),
    module     VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: PermissionGroup
CREATE TABLE n_permission_group (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(255)    NOT NULL UNIQUE,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: UserRoleMapping
CREATE TABLE n_user_role_mapping (
    id         BIGSERIAL       PRIMARY KEY,
    username   VARCHAR(255)    NOT NULL,
    role       VARCHAR(255)    NOT NULL,
    is_primary BOOLEAN         NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: RolePermissionMapping
CREATE TABLE n_role_permission_mapping (
    id            BIGSERIAL       PRIMARY KEY,
    role          VARCHAR(255)    NOT NULL,
    permission_id BIGINT          NOT NULL,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);

-- Entity: RolePermissionGroupMapping
CREATE TABLE n_role_permission_group_mapping (
    id                  BIGSERIAL       PRIMARY KEY,
    role                VARCHAR(255)    NOT NULL,
    permission_group_id BIGINT          NOT NULL,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT          DEFAULT 0
);

-- Entity: PermissionGroupMapping
CREATE TABLE n_permission_group_mapping (
    id                  BIGSERIAL       PRIMARY KEY,
    permission_id       BIGINT          NOT NULL,
    permission_group_id BIGINT          NOT NULL,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: master (codemaster)
-- =============================================================================

-- Entity: MasterCode
CREATE TABLE n_master_code (
    id                BIGSERIAL       PRIMARY KEY,
    parent_id         BIGINT,
    key               VARCHAR(100)    NOT NULL,
    name              JSONB,
    description       JSONB,
    is_system_defined BOOLEAN         NOT NULL,
    created_by        VARCHAR(255),
    created_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    version           BIGINT          DEFAULT 0
);

-- Entity: MasterCodeValue
CREATE TABLE n_master_code_value (
    id            BIGSERIAL       PRIMARY KEY,
    key           VARCHAR(100)    NOT NULL UNIQUE,
    code_key      VARCHAR(100)    NOT NULL,
    value         JSONB,
    description   JSONB,
    is_active     BOOLEAN         NOT NULL,
    display_order INTEGER         NOT NULL,
    icons         JSONB,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);

-- Entity: Product
CREATE TABLE n_product (
    id         BIGSERIAL       PRIMARY KEY,
    code       VARCHAR(50)     NOT NULL UNIQUE,
    name       JSONB           NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: master (location)
-- =============================================================================

-- Entity: Country
CREATE TABLE n_master_country (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(255)    NOT NULL,
    code       VARCHAR(50),
    is_active  BOOLEAN         NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: State
CREATE TABLE n_master_state (
    id         BIGSERIAL       PRIMARY KEY,
    country_id BIGINT          NOT NULL,
    name       VARCHAR(255)    NOT NULL,
    code       VARCHAR(50),
    is_active  BOOLEAN         NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0,
    CONSTRAINT fk_state_country FOREIGN KEY (country_id) REFERENCES n_master_country (id)
);

-- Entity: District
CREATE TABLE n_master_district (
    id            BIGSERIAL       PRIMARY KEY,
    state_id      BIGINT          NOT NULL,
    name          VARCHAR(255)    NOT NULL,
    code          VARCHAR(50),
    value         JSONB,
    is_active     BOOLEAN         NOT NULL,
    display_order INTEGER         NOT NULL,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0,
    CONSTRAINT fk_district_state FOREIGN KEY (state_id) REFERENCES n_master_state (id)
);

-- Entity: Taluka
CREATE TABLE n_master_taluka (
    id            BIGSERIAL       PRIMARY KEY,
    district_id   BIGINT          NOT NULL,
    name          VARCHAR(255)    NOT NULL,
    code          VARCHAR(50),
    value         JSONB,
    is_active     BOOLEAN         NOT NULL,
    display_order INTEGER         NOT NULL,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0,
    CONSTRAINT fk_taluka_district FOREIGN KEY (district_id) REFERENCES n_master_district (id)
);

-- Entity: Village
CREATE TABLE n_master_village (
    id         BIGSERIAL       PRIMARY KEY,
    taluka_id  BIGINT          NOT NULL,
    name       VARCHAR(255)    NOT NULL,
    code       VARCHAR(50),
    value      JSONB,
    is_active  BOOLEAN         NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0,
    CONSTRAINT fk_village_taluka FOREIGN KEY (taluka_id) REFERENCES n_master_taluka (id)
);

-- Entity: Pincode
CREATE TABLE n_master_pincode (
    id           BIGSERIAL       PRIMARY KEY,
    pincode      VARCHAR(10)     NOT NULL,
    country_id   BIGINT,
    state_id     BIGINT,
    district_id  BIGINT,
    taluka_id    BIGINT,
    is_servicable BOOLEAN        NOT NULL,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP,
    version      BIGINT          DEFAULT 0
);

-- Entity: PincodeValuationMaster
CREATE TABLE n_master_pincode_valuation (
    id                BIGSERIAL       PRIMARY KEY,
    pincode           VARCHAR(6)      NOT NULL UNIQUE,
    tier              VARCHAR(50),
    form3_valuation   NUMERIC,
    valuation_11a     NUMERIC,
    valuation_11b     NUMERIC,
    created_by        VARCHAR(255),
    created_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    version           BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: offices
-- =============================================================================

-- Entity: Office
CREATE TABLE n_office (
    id           BIGSERIAL       PRIMARY KEY,
    name         VARCHAR(255)    NOT NULL,
    key          VARCHAR(255)    NOT NULL UNIQUE,
    code         VARCHAR(255)    NOT NULL UNIQUE,
    address_data JSONB,
    parent_id    BIGINT,
    is_active    BOOLEAN,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP,
    version      BIGINT          DEFAULT 0,
    CONSTRAINT fk_office_parent FOREIGN KEY (parent_id) REFERENCES n_office (id)
);

-- =============================================================================
-- MODULE: lead
-- =============================================================================

-- Entity: Lead
CREATE TABLE n_lead (
    id                        BIGSERIAL       PRIMARY KEY,
    lead_identifier           UUID            NOT NULL UNIQUE,
    requested_amount          NUMERIC(18, 2),
    office_key                VARCHAR(255)    NOT NULL,
    contacts                  JSONB,
    applicant                 BIGINT,
    co_applicants             JSONB,
    workflow_details          JSONB,
    purpose                   VARCHAR(255),
    product_code              VARCHAR(100),
    customer_convince_status  VARCHAR(100),
    status                    VARCHAR(100),
    substatus                 VARCHAR(100),
    owner                     VARCHAR(255),
    preliminary_details       JSONB,
    document_details          JSONB,
    notes                     JSONB,
    credit_rating_details     JSONB,
    disbursement_details      JSONB,
    reasons                   JSONB,
    sourcing_channel_id       BIGINT,
    proposed_details          JSONB,
    other_details             JSONB,
    onhold_details            JSONB,
    rejection_details         JSONB,
    withdrawn_details         JSONB,
    dropoff_details           JSONB,
    external_ids              JSONB,
    income_obligation_details JSONB,
    call_summary_details      JSONB,
    task_timeline             JSONB,
    bre_executions            JSONB,
    created_by                VARCHAR(255),
    created_at                TIMESTAMP,
    updated_by                VARCHAR(255),
    updated_at                TIMESTAMP,
    version                   BIGINT          DEFAULT 0
);

-- Entity: Applicant
CREATE TABLE n_applicant (
    id            BIGSERIAL       PRIMARY KEY,
    identifier    UUID            NOT NULL UNIQUE,
    person_id     BIGINT          NOT NULL,
    referral_code VARCHAR(255)    NOT NULL UNIQUE,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);

-- Entity: Contact
CREATE TABLE n_contact (
    id              BIGSERIAL       PRIMARY KEY,
    identifier      UUID            NOT NULL UNIQUE,
    person_id       BIGINT          NOT NULL,
    decision_maker  BOOLEAN         NOT NULL,
    property_owner  BOOLEAN         NOT NULL,
    cb_enquiry_id   JSONB,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- Entity: QueueConfig (lead routing / ordering)
CREATE TABLE n_queue_config (
    id                  BIGSERIAL       PRIMARY KEY,
    queue_name          VARCHAR(255)    NOT NULL UNIQUE,
    description         VARCHAR(500),
    data_providers      JSONB           NOT NULL,
    user_ids            JSONB           NOT NULL,
    lock_duration       INT             NOT NULL DEFAULT 10,
    reorder_time        INT             NOT NULL DEFAULT 5,
    last_reorder_time   TIMESTAMP,
    is_active           BOOLEAN         NOT NULL DEFAULT true,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT          DEFAULT 0
);

-- Entity: LeadQueue (per-queue lead order and claim state)
CREATE TABLE n_lead_queue (
    id                      BIGSERIAL       PRIMARY KEY,
    queue_config_id         BIGINT          NOT NULL,
    lead_id                 BIGINT          NOT NULL,
    calculated_at           TIMESTAMP       NOT NULL,
    position                INT             NOT NULL,
    currently_claimed_by    VARCHAR(255),
    claim_expiry_at         TIMESTAMP,
    claimed_history         JSONB,
    is_active               BOOLEAN         NOT NULL DEFAULT true,
    created_by              VARCHAR(255),
    created_at              TIMESTAMP,
    updated_by              VARCHAR(255),
    updated_at              TIMESTAMP,
    version                 BIGINT          DEFAULT 0,
    CONSTRAINT fk_n_lead_queue_queue_config FOREIGN KEY (queue_config_id) REFERENCES n_queue_config (id),
    CONSTRAINT fk_n_lead_queue_lead FOREIGN KEY (lead_id) REFERENCES n_lead (id)
);

-- Staging table (reorder job; JDBC bulk / temp positions)
CREATE TABLE n_lead_queue_temp (
    queue_config_id     BIGINT          NOT NULL,
    lead_id             BIGINT          NOT NULL,
    position            INT             NOT NULL,
    PRIMARY KEY (queue_config_id, lead_id),
    CONSTRAINT fk_n_lead_queue_temp_queue_config FOREIGN KEY (queue_config_id) REFERENCES n_queue_config (id) ON DELETE CASCADE,
    CONSTRAINT fk_n_lead_queue_temp_lead FOREIGN KEY (lead_id) REFERENCES n_lead (id) ON DELETE CASCADE
);

CREATE INDEX idx_n_lead_queue_config_active_pos ON n_lead_queue (queue_config_id, is_active, position);
CREATE INDEX idx_n_lead_queue_lead_id ON n_lead_queue (lead_id);
CREATE INDEX idx_n_queue_config_user_ids_gin ON n_queue_config USING GIN (user_ids);
CREATE INDEX idx_n_lead_queue_temp_qid ON n_lead_queue_temp (queue_config_id);

-- =============================================================================
-- MODULE: leadstages
-- =============================================================================

-- Entity: LeadStageHistory
CREATE TABLE n_lead_stage_history (
    id            BIGSERIAL       PRIMARY KEY,
    lead_id       BIGINT          NOT NULL,
    stage_key     VARCHAR(100)    NOT NULL,
    stage_from    VARCHAR(100),
    sub_stage_key VARCHAR(100),
    entered_at    TIMESTAMP       NOT NULL,
    exited_at     TIMESTAMP,
    moved_by      VARCHAR(255)    NOT NULL,
    remarks       TEXT,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);

-- Entity: LeadStageAssignmentHistory
CREATE TABLE n_lead_stage_assignment_history (
    id               BIGSERIAL       PRIMARY KEY,
    stage_history_id BIGINT          NOT NULL,
    assigned_to      VARCHAR(255)    NOT NULL,
    assigned_by      VARCHAR(255),
    assigned_at      TIMESTAMP       NOT NULL,
    unassigned_at    TIMESTAMP,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP,
    version          BIGINT          DEFAULT 0,
    CONSTRAINT fk_stage_assignment_stage_history
        FOREIGN KEY (stage_history_id) REFERENCES n_lead_stage_history (id)
);

-- =============================================================================
-- MODULE: leadlender
-- =============================================================================

-- Entity: LeadLender
CREATE TABLE n_lead_lender (
    id                 BIGSERIAL       PRIMARY KEY,
    lender_identifier  UUID            NOT NULL UNIQUE,
    lead_id            BIGINT          NOT NULL,
    lender_key         VARCHAR(20)     NOT NULL,
    status             VARCHAR(40)     NOT NULL,
    lender_office_key  VARCHAR(20),
    login_details      JSONB,
    rm_details         JSONB,
    approved_details   JSONB,
    stage              VARCHAR(100),
    rejection_details  JSONB,
    created_by         VARCHAR(255),
    created_at         TIMESTAMP,
    updated_by         VARCHAR(255),
    updated_at         TIMESTAMP,
    version            BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: leadbre
-- =============================================================================

-- Entity: LeadBREResult
CREATE TABLE n_lead_bre_results (
    id          BIGSERIAL       PRIMARY KEY,
    identifier  UUID            NOT NULL UNIQUE,
    lead_id     BIGINT          NOT NULL,
    config_name VARCHAR(255)    NOT NULL,
    status      VARCHAR(32)     NOT NULL,
    input       VARCHAR(255),
    output      VARCHAR(255),
    created_by  VARCHAR(255),
    created_at  TIMESTAMP,
    updated_by  VARCHAR(255),
    updated_at  TIMESTAMP,
    version     BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: leadactivity
-- =============================================================================

-- Entity: LeadActivity
CREATE TABLE n_lead_activity (
    id              BIGSERIAL       PRIMARY KEY,
    identifier      UUID            NOT NULL UNIQUE,
    lead_id         BIGINT          NOT NULL,
    resource_type   VARCHAR(255),
    resource_action VARCHAR(255),
    resource_id     BIGINT,
    description     VARCHAR(255),
    metadata        JSONB,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: advisor
-- =============================================================================

-- Entity: Advisor
CREATE TABLE n_advisor (
    id                     BIGSERIAL       PRIMARY KEY,
    identifier             UUID            NOT NULL UNIQUE,
    username               VARCHAR(255),
    status                 VARCHAR(50)     NOT NULL,
    remarks                JSONB,
    bank_details           JSONB,
    qualification_details  JSONB,
    other_details          JSONB,
    segmentation_details   JSONB,
    source_channel_id      BIGINT,
    office_key             VARCHAR(100),
    owner                  VARCHAR(255),
    rejection_details      JSONB,
    notes                  JSONB,
    external_ids           JSONB,
    call_logs              JSONB,
    referral_code          VARCHAR(255)    NOT NULL UNIQUE,
    created_by             VARCHAR(255),
    created_at             TIMESTAMP,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMP,
    version                BIGINT          DEFAULT 0
);

-- Entity: AdvisorActivity
CREATE TABLE n_advisor_activity (
    id              BIGSERIAL       PRIMARY KEY,
    identifier      UUID            NOT NULL UNIQUE,
    advisor_id      BIGINT          NOT NULL,
    resource_type   VARCHAR(255),
    resource_action VARCHAR(255),
    resource_id     BIGINT,
    description     VARCHAR(255),
    metadata        JSONB,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: staff
-- =============================================================================

-- Entity: Staff
CREATE TABLE n_staff (
    id            BIGSERIAL       PRIMARY KEY,
    identifier    UUID            NOT NULL UNIQUE,
    user_id       BIGINT          NOT NULL,
    office_key    VARCHAR(20)     NOT NULL,
    referral_code VARCHAR(255)    NOT NULL UNIQUE,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: lender
-- =============================================================================

-- Entity: Lender
CREATE TABLE n_lender (
    id         UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    key        VARCHAR(20)     NOT NULL,
    name       VARCHAR(100)    NOT NULL,
    status     VARCHAR(40)     NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: LenderOffice
CREATE TABLE n_lender_office (
    id         UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100)    NOT NULL,
    key        VARCHAR(100)    NOT NULL,
    lender_key VARCHAR(20)     NOT NULL,
    address    JSONB,
    status     VARCHAR(40)     NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: stage
-- =============================================================================

-- Entity: StageConfig
CREATE TABLE n_stage_config (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    key             VARCHAR(100)    NOT NULL UNIQUE,
    description     VARCHAR(500),
    stage_config    JSONB,
    assignee_roles  JSONB,
    sub_stages_code VARCHAR(100),
    is_active       BOOLEAN,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: workflow
-- =============================================================================

-- Entity: WorkflowConfig
CREATE TABLE n_workflow_config (
    id                      BIGSERIAL       PRIMARY KEY,
    workflow_config_key     VARCHAR(100)    NOT NULL UNIQUE,
    name                    VARCHAR(100)    NOT NULL,
    description             VARCHAR(255),
    workflow_config_details JSONB,
    is_active               BOOLEAN,
    created_by              VARCHAR(255),
    created_at              TIMESTAMP,
    updated_by              VARCHAR(255),
    updated_at              TIMESTAMP,
    version                 BIGINT          DEFAULT 0
);

-- Entity: PendingWorkflowAction
CREATE TABLE n_pending_workflow_actions (
    id                      BIGSERIAL       PRIMARY KEY,
    action_identifier       UUID            NOT NULL UNIQUE,
    entity_identifier       UUID            NOT NULL,
    entity_type             VARCHAR(50)     NOT NULL,
    current_stage_key       VARCHAR(100)    NOT NULL,
    source_task_identifier  UUID            NOT NULL,
    source_task_config_key  VARCHAR(100)    NOT NULL,
    source_outcome          VARCHAR(100)    NOT NULL,
    action_details          JSONB           NOT NULL,
    status                  VARCHAR(50)     NOT NULL,
    executed_at             TIMESTAMP,
    executed_by             VARCHAR(255),
    created_by              VARCHAR(255),
    created_at              TIMESTAMP,
    updated_by              VARCHAR(255),
    updated_at              TIMESTAMP,
    version                 BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: task
-- =============================================================================

-- Entity: Task
CREATE TABLE n_tasks (
    id              BIGSERIAL       PRIMARY KEY,
    task_identifier UUID            NOT NULL UNIQUE,
    task_config_key VARCHAR(100)    NOT NULL,
    name            VARCHAR(100),
    assigned_to     VARCHAR(255),
    due_at          TIMESTAMP,
    outcome         VARCHAR(100),
    outcome_details JSONB,
    task_details    JSONB,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- Open LEAD tasks: lookup n_tasks from n_lead via task_details->>entityId (used by LATERAL queue / dashboard)
CREATE INDEX idx_n_tasks_lead_entity_open
    ON n_tasks ( ((task_details->>'entityId')::uuid) )
    WHERE outcome IS NULL
      AND (task_details->>'entityType') = 'LEAD';

-- Entity: TaskConfig
CREATE TABLE n_task_config (
    id                  BIGSERIAL       PRIMARY KEY,
    task_config_key     VARCHAR(100)    NOT NULL UNIQUE,
    name                VARCHAR(100)    NOT NULL,
    description         VARCHAR(255),
    task_config_details JSONB,
    is_active           BOOLEAN,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,
    version             BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: bre
-- =============================================================================

-- Entity: BREConfigs
CREATE TABLE n_bre_configs (
    id         BIGSERIAL       PRIMARY KEY,
    uname      VARCHAR(255)    NOT NULL UNIQUE,
    config     JSONB           NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: BRELogs
CREATE TABLE n_bre_log (
    id         BIGSERIAL       PRIMARY KEY,
    data_ext   VARCHAR(255),
    config_id  BIGINT,
    request    VARCHAR(255),
    response   VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: dataprovider
-- =============================================================================

-- Entity: DataProvider
CREATE TABLE n_data_provider (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(255)    NOT NULL UNIQUE,
    query      TEXT            NOT NULL,
    status     VARCHAR(50)     NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: document
-- =============================================================================

-- Entity: Document
CREATE TABLE n_document (
    id         BIGSERIAL       PRIMARY KEY,
    identifier UUID            NOT NULL UNIQUE,
    name       VARCHAR(255)    NOT NULL,
    type       VARCHAR(255),
    size       BIGINT,
    provider   VARCHAR(255)    NOT NULL,
    path       VARCHAR(255)    NOT NULL,
    data_ext   JSONB,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: consent
-- =============================================================================

-- Entity: Consent
CREATE TABLE n_consent (
    id                        BIGSERIAL       PRIMARY KEY,
    identifier                UUID            NOT NULL UNIQUE,
    status                    VARCHAR(50),
    consent_sent_details      JSONB,
    consent_received_details  JSONB,
    consent_withdrawn_details JSONB,
    created_by                VARCHAR(255),
    created_at                TIMESTAMP,
    updated_by                VARCHAR(255),
    updated_at                TIMESTAMP,
    version                   BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: notes
-- =============================================================================

-- Entity: Notes
CREATE TABLE n_note (
    id         BIGSERIAL       PRIMARY KEY,
    identifier UUID            NOT NULL UNIQUE,
    title      VARCHAR(255)    NOT NULL,
    content    TEXT            NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: referral
-- =============================================================================

-- Entity: ReferralCodeRegistry
CREATE TABLE n_referral_code_registry (
    id                BIGSERIAL       PRIMARY KEY,
    referral_code     VARCHAR(255)    UNIQUE,
    entity_type       VARCHAR(255),
    entity_identifier UUID,
    created_by        VARCHAR(255),
    created_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    version           BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: marketing
-- =============================================================================

-- Entity: SourcingChannel
CREATE TABLE n_sourcing_channel_details (
    id                    BIGSERIAL       PRIMARY KEY,
    sourcing_identifier   UUID            NOT NULL UNIQUE,
    sourcing_channel_name VARCHAR(255),
    marketing_source      VARCHAR(255),
    marketing_details     JSONB,
    created_by            VARCHAR(255),
    created_at            TIMESTAMP,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMP,
    version               BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: notifications
-- =============================================================================

-- Entity: NotificationConfig
CREATE TABLE n_notification_config (
    id         BIGSERIAL       PRIMARY KEY,
    identifier UUID            NOT NULL UNIQUE,
    name       VARCHAR(255)    NOT NULL,
    config     JSONB           NOT NULL,
    status     VARCHAR(50)     NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: NotificationEventMapping
CREATE TABLE n_notification_event_mapping (
    id                     BIGSERIAL       PRIMARY KEY,
    event                  VARCHAR(100)    NOT NULL,
    notification_config_id BIGINT          NOT NULL,
    status                 VARCHAR(50)     NOT NULL,
    created_by             VARCHAR(255),
    created_at             TIMESTAMP,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMP,
    version                BIGINT          DEFAULT 0,
    CONSTRAINT fk_event_mapping_config
        FOREIGN KEY (notification_config_id) REFERENCES n_notification_config (id)
);

-- Entity: NotificationTemplate
CREATE TABLE n_notification_template (
    id         BIGSERIAL       PRIMARY KEY,
    identifier VARCHAR(255)    NOT NULL UNIQUE,
    channel    VARCHAR(50),
    mode       VARCHAR(50),
    detail     TEXT,
    variables  JSONB,
    buttons    JSONB,
    status     VARCHAR(50)     NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: NotificationRecord
CREATE TABLE n_notification_record (
    id                     UUID            NOT NULL PRIMARY KEY,
    notification_config_id BIGINT          NOT NULL,
    idempotency_key        VARCHAR(512)    UNIQUE,
    notification_payload   JSONB,
    details                JSONB,
    status                 VARCHAR(50)     NOT NULL,
    error_json             JSONB,
    created_by             VARCHAR(255),
    created_at             TIMESTAMP,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMP,
    version                BIGINT          DEFAULT 0
);

-- Entity: NotificationReceipt
CREATE TABLE n_notification_receipt (
    id                     UUID            NOT NULL PRIMARY KEY,
    notification_record_id UUID            NOT NULL,
    mode                   VARCHAR(50),
    recipient_contact      VARCHAR(255),
    channel_type           VARCHAR(50),
    template_identifier    VARCHAR(255),
    message_payload        JSONB,
    status                 VARCHAR(50)     NOT NULL,
    details                JSONB,
    schedules              JSONB,
    remarks                JSONB,
    error_json             JSONB,
    created_by             VARCHAR(255),
    created_at             TIMESTAMP,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMP,
    version                BIGINT          DEFAULT 0
);

-- Entity: Device
CREATE TABLE n_device (
    id                           BIGSERIAL               PRIMARY KEY,
    app_user                     VARCHAR(50)             NOT NULL,
    notification_token           VARCHAR(500)            NOT NULL,
    device_id                    VARCHAR(255)            NOT NULL,
    platform                     VARCHAR(50)             NOT NULL,
    app_version                  VARCHAR(50),
    os_version                   VARCHAR(50),
    device_model                 VARCHAR(100),
    apk_version                  VARCHAR(50),
    sdk_version                  VARCHAR(50),
    is_active                    BOOLEAN                 NOT NULL,
    last_registration_token_time TIMESTAMP WITH TIME ZONE,
    created_by                   VARCHAR(255),
    created_at                   TIMESTAMP,
    updated_by                   VARCHAR(255),
    updated_at                   TIMESTAMP,
    version                      BIGINT                  DEFAULT 0
);

-- Entity: AppNotification
CREATE TABLE n_app_notification (
    id                     BIGSERIAL               PRIMARY KEY,
    device_id              BIGINT                  NOT NULL,
    receipt_id             UUID                    NOT NULL,
    notification_record_id UUID                    NOT NULL,
    notification_token     VARCHAR(500),
    template_id            VARCHAR(255),
    template_name          VARCHAR(255),
    provider_message_id    VARCHAR(500),
    status                 VARCHAR(50)             NOT NULL,
    error_code             VARCHAR(100),
    error_message          TEXT,
    sent_timestamp         TIMESTAMP WITH TIME ZONE,
    opened_at              TIMESTAMP WITH TIME ZONE,
    created_by             VARCHAR(255),
    created_at             TIMESTAMP,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMP,
    version                BIGINT                  DEFAULT 0
);

-- Entity: LeadWhatsAppNotification
CREATE TABLE n_lead_whatsapp_notification (
    id                       BIGSERIAL               PRIMARY KEY,
    lead_identifier          UUID                    NOT NULL,
    receipt_id               UUID                    NOT NULL,
    notification_record_id   UUID                    NOT NULL,
    template_id              VARCHAR(255),
    template_name            VARCHAR(255),
    local_message_id         VARCHAR(255)            UNIQUE,
    whatsapp_message_id      VARCHAR(500),
    wa_id                    VARCHAR(50),
    status                   VARCHAR(50),
    is_replied               BOOLEAN,
    reply_text               TEXT,
    reply_timestamp          TIMESTAMP WITH TIME ZONE,
    delivered_timestamp      TIMESTAMP WITH TIME ZONE,
    sent_timestamp           TIMESTAMP WITH TIME ZONE,
    conversation_id          VARCHAR(255),
    ticket_id                VARCHAR(255),
    error_message            TEXT,
    is_valid_whatsapp_number BOOLEAN,
    status_track             JSONB,
    created_by               VARCHAR(255),
    created_at               TIMESTAMP,
    updated_by               VARCHAR(255),
    updated_at               TIMESTAMP,
    version                  BIGINT                  DEFAULT 0
);

-- Entity: AdvisorWhatsAppNotification
CREATE TABLE n_advisor_whatsapp_notification (
    id                       BIGSERIAL               PRIMARY KEY,
    advisor_identifier       UUID                    NOT NULL,
    receipt_id               UUID                    NOT NULL,
    notification_record_id   UUID                    NOT NULL,
    template_id              VARCHAR(255),
    template_name            VARCHAR(255),
    local_message_id         VARCHAR(255)            UNIQUE,
    whatsapp_message_id      VARCHAR(500),
    wa_id                    VARCHAR(50),
    status                   VARCHAR(50),
    is_replied               BOOLEAN,
    reply_text               TEXT,
    reply_timestamp          TIMESTAMP WITH TIME ZONE,
    delivered_timestamp      TIMESTAMP WITH TIME ZONE,
    sent_timestamp           TIMESTAMP WITH TIME ZONE,
    conversation_id          VARCHAR(255),
    ticket_id                VARCHAR(255),
    error_message            TEXT,
    is_valid_whatsapp_number BOOLEAN,
    status_track             JSONB,
    created_by               VARCHAR(255),
    created_at               TIMESTAMP,
    updated_by               VARCHAR(255),
    updated_at               TIMESTAMP,
    version                  BIGINT                  DEFAULT 0
);

-- =============================================================================
-- MODULE: creditbureau
-- =============================================================================

-- Entity: CbConfig
CREATE TABLE n_cb_config (
    id           BIGSERIAL       PRIMARY KEY,
    config_key   VARCHAR(100)    NOT NULL UNIQUE,
    config_value JSONB,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP,
    version      BIGINT          DEFAULT 0
);

-- Entity: CreditBureauEnquiry
CREATE TABLE n_cb_enquiry (
    id             BIGSERIAL       PRIMARY KEY,
    identifier     UUID            NOT NULL UNIQUE,
    report_id      VARCHAR(255),
    status         VARCHAR(50),
    consent_id     BIGINT,
    provider       VARCHAR(100),
    error          TEXT,
    request_json   TEXT,
    response_json  TEXT,
    report_details JSONB,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP,
    version        BIGINT          DEFAULT 0
);

-- Entity: CreditBureauCustomerEnquiry
CREATE TABLE n_cb_customer_enquiry (
    id                          BIGSERIAL       PRIMARY KEY,
    identifier                  UUID            NOT NULL UNIQUE,
    enquiry_id                  BIGINT          NOT NULL,
    lender_name                 VARCHAR(200),
    inquiry_date                DATE,
    ownership_type              VARCHAR(50),
    credit_inquiry_purpose_type VARCHAR(100),
    inquiry_amount              NUMERIC(18,2),
    created_by                  VARCHAR(255),
    created_at                  TIMESTAMP,
    updated_by                  VARCHAR(255),
    updated_at                  TIMESTAMP,
    version                     BIGINT          DEFAULT 0
);

-- Entity: CreditBureauSummary
CREATE TABLE n_cb_summary (
    id                             BIGSERIAL       PRIMARY KEY,
    identifier                     UUID            NOT NULL UNIQUE,
    enquiry_id                     BIGINT          NOT NULL,
    credit_score                   INTEGER,
    score_version                  VARCHAR(50),
    score_name                     VARCHAR(255),
    total_accounts                 INTEGER,
    active_accounts                INTEGER,
    overdue_accounts               INTEGER,
    closed_accounts                INTEGER,
    secured_accounts               INTEGER,
    unsecured_accounts             INTEGER,
    untagged_accounts              INTEGER,
    total_current_balance          NUMERIC(18,2),
    current_balance_secured        NUMERIC(18,2),
    current_balance_unsecured      NUMERIC(18,2),
    total_overdue_amount           NUMERIC(18,2),
    total_sanctioned_amount        NUMERIC(18,2),
    total_disbursed_amount         NUMERIC(18,2),
    no_of_own_mfis                 INTEGER,
    no_of_other_mfis               INTEGER,
    total_own_current_balance      NUMERIC(18,2),
    total_own_installment_amount   NUMERIC(18,2),
    total_own_disbursed_amount     NUMERIC(18,2),
    total_other_installment_amount NUMERIC(18,2),
    total_other_disbursed_amount   NUMERIC(18,2),
    total_other_overdue_amount     NUMERIC(18,2),
    max_worst_delinquency          INTEGER,
    account_count                  INTEGER,
    score_factor_details           JSONB,
    created_by                     VARCHAR(255),
    created_at                     TIMESTAMP,
    updated_by                     VARCHAR(255),
    updated_at                     TIMESTAMP,
    version                        BIGINT          DEFAULT 0
);

-- Entity: CreditBureauTradeline
CREATE TABLE n_cb_customer_tradeline (
    id                               BIGSERIAL       PRIMARY KEY,
    identifier                       UUID            NOT NULL UNIQUE,
    enquiry_id                       BIGINT          NOT NULL,
    account_number                   VARCHAR(100),
    credit_grantor                   VARCHAR(200),
    credit_grantor_group             VARCHAR(50),
    credit_grantor_type              VARCHAR(10),
    account_type                     VARCHAR(100),
    account_status                   VARCHAR(50),
    reported_date                    DATE,
    closed_date                      DATE,
    ownership_type                   VARCHAR(50),
    disbursed_amount                 NUMERIC(18,2),
    disbursed_date                   DATE,
    current_balance                  NUMERIC(18,2),
    credit_limit                     NUMERIC(18,2),
    cash_limit                       NUMERIC(18,2),
    overdue_amount                   NUMERIC(18,2),
    installment_amount               VARCHAR(50),
    installment_frequency            VARCHAR(100),
    original_term                    INTEGER,
    term_to_maturity                 INTEGER,
    repayment_tenure                 VARCHAR(50),
    interest_rate                    VARCHAR(10),
    last_payment_date                TIMESTAMP,
    last_paid_amount                 NUMERIC(18,2),
    actual_payment                   NUMERIC(18,2),
    write_off_amount                 NUMERIC(18,2),
    principal_write_off_amount       NUMERIC(18,2),
    settlement_amount                NUMERIC(18,2),
    write_off_date                   DATE,
    security_status                  VARCHAR(100),
    obligation                       NUMERIC(18,2),
    account_remarks                  TEXT,
    account_in_dispute               BOOLEAN,
    suit_filed_wilful_default_status VARCHAR(100),
    written_off_settled_status       VARCHAR(100),
    suit_filed_date                  DATE,
    occupation                       VARCHAR(100),
    income_frequency                 VARCHAR(50),
    income_amount                    NUMERIC(18,2),
    created_by                       VARCHAR(255),
    created_at                       TIMESTAMP,
    updated_by                       VARCHAR(255),
    updated_at                       TIMESTAMP,
    version                          BIGINT          DEFAULT 0
);

-- Entity: CreditBureauTrends
CREATE TABLE n_cb_trend (
    id          BIGSERIAL       PRIMARY KEY,
    identifier  UUID            NOT NULL UNIQUE,
    enquiry_id  BIGINT          NOT NULL,
    trend_name  VARCHAR(255),
    date        DATE,
    score_value INTEGER,
    description TEXT,
    created_by  VARCHAR(255),
    created_at  TIMESTAMP,
    updated_by  VARCHAR(255),
    updated_at  TIMESTAMP,
    version     BIGINT          DEFAULT 0
);

-- Entity: CreditBureauAttribute
CREATE TABLE n_cb_attribute (
    id         BIGSERIAL       PRIMARY KEY,
    enquiry_id BIGINT          NOT NULL,
    category   VARCHAR(50)     NOT NULL,
    attr_name  VARCHAR(100)    NOT NULL,
    attr_value VARCHAR(100),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: CreditBureauDerivedAttribute
CREATE TABLE n_cb_derived_attribute (
    id         BIGSERIAL       PRIMARY KEY,
    enquiry_id BIGINT          NOT NULL,
    attr_name  VARCHAR(200)    NOT NULL,
    attr_value TEXT,
    data_ext   JSONB,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: CreditBureauDemographicVariation
CREATE TABLE n_cb_demographic_variation (
    id                   BIGSERIAL       PRIMARY KEY,
    identifier           UUID            NOT NULL UNIQUE,
    enquiry_id           BIGINT          NOT NULL,
    variation_type       VARCHAR(50),
    variation_value      TEXT,
    reported_date        DATE,
    first_reported_date  DATE,
    loan_type_associated TEXT,
    source_indicator     VARCHAR(50),
    created_by           VARCHAR(255),
    created_at           TIMESTAMP,
    updated_by           VARCHAR(255),
    updated_at           TIMESTAMP,
    version              BIGINT          DEFAULT 0
);

-- Entity: CreditBureauAccountSecurityDetails
CREATE TABLE n_cb_account_security_detail (
    id                    BIGSERIAL       PRIMARY KEY,
    identifier            UUID            NOT NULL UNIQUE,
    tradeline_id          BIGINT          NOT NULL,
    security_type         VARCHAR(100),
    owner_name            VARCHAR(200),
    security_valuation    NUMERIC(18,2),
    date_of_valuation     DATE,
    security_charge       VARCHAR(100),
    property_address      TEXT,
    automobile_type       VARCHAR(100),
    year_of_manufacturing INTEGER,
    registration_number   VARCHAR(50),
    engine_number         VARCHAR(50),
    chassis_number        VARCHAR(50),
    created_by            VARCHAR(255),
    created_at            TIMESTAMP,
    updated_by            VARCHAR(255),
    updated_at            TIMESTAMP,
    version               BIGINT          DEFAULT 0
);

-- Entity: CreditBureauAccountPaymentHistory
CREATE TABLE n_cb_account_payment_history (
    id            BIGSERIAL       PRIMARY KEY,
    identifier    UUID            NOT NULL UNIQUE,
    tradeline_id  BIGINT          NOT NULL,
    history_type  VARCHAR(50),
    month_year    VARCHAR(10),
    history_value VARCHAR(20),
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: call
-- =============================================================================

-- Entity: CallLog
CREATE TABLE n_call_log (
    id                 BIGSERIAL       PRIMARY KEY,
    identifier         UUID            NOT NULL UNIQUE,
    provider           VARCHAR(100)    NOT NULL,
    provider_id        VARCHAR(255)    NOT NULL,
    caller_id          VARCHAR(255),
    from_number        VARCHAR(50),
    to_number          VARCHAR(50),
    direction          VARCHAR(50),
    source             VARCHAR(50),
    status             VARCHAR(50),
    recording_details  JSONB,
    completion_details JSONB,
    campaign_id        BIGINT,
    ai_analysis        JSONB,
    created_by         VARCHAR(255),
    created_at         TIMESTAMP,
    updated_by         VARCHAR(255),
    updated_at         TIMESTAMP,
    version            BIGINT          DEFAULT 0
);

-- Entity: CallLogLead
CREATE TABLE n_call_log_lead (
    call_log_id BIGINT          NOT NULL PRIMARY KEY,
    lead_id     BIGINT          NOT NULL,
    contact_id  BIGINT,
    created_by  VARCHAR(255),
    created_at  TIMESTAMP,
    updated_by  VARCHAR(255),
    updated_at  TIMESTAMP,
    version     BIGINT          DEFAULT 0
);

-- Entity: ReconciliationLog
CREATE TABLE n_reconciliation_log (
    id                BIGSERIAL       PRIMARY KEY,
    call_log_id       BIGINT          NOT NULL,
    exotel_call_sid   VARCHAR(255),
    changes           JSONB,
    correction_source VARCHAR(64)     NOT NULL,
    created_by        VARCHAR(255),
    created_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    version           BIGINT          DEFAULT 0
);

-- Entity: RoleCallConfigs
CREATE TABLE n_role_call_configs (
    id         BIGSERIAL       PRIMARY KEY,
    identifier UUID            NOT NULL UNIQUE,
    role       VARCHAR(255)    NOT NULL,
    "callerId" VARCHAR(255)    NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: campaign
-- =============================================================================

-- Entity: Campaign
CREATE TABLE n_campaign (
    id               BIGSERIAL       PRIMARY KEY,
    identifier       UUID            NOT NULL UNIQUE,
    config_id        BIGINT          NOT NULL,
    provider         VARCHAR(100),
    provider_id      VARCHAR(100),
    provider_details JSONB,
    name             VARCHAR(255)    NOT NULL,
    status           VARCHAR(50)     NOT NULL,
    document_details JSONB,
    summary          JSONB,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP,
    version          BIGINT          DEFAULT 0
);

-- Entity: CampaignConfig
CREATE TABLE n_campaign_config (
    id         BIGSERIAL       PRIMARY KEY,
    identifier UUID            NOT NULL UNIQUE,
    name       VARCHAR(255)    NOT NULL,
    config     JSONB           NOT NULL,
    status     VARCHAR(50)     NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: bulk-operations
-- =============================================================================

-- Entity: BulkOperation
CREATE TABLE n_bulk_operation (
    id                            BIGSERIAL       PRIMARY KEY,
    operation_identifier          UUID            NOT NULL UNIQUE,
    operation_type                VARCHAR(50)     NOT NULL,
    status                        VARCHAR(50)     NOT NULL,
    file_name                     VARCHAR(255),
    file_size                     BIGINT,
    file_hash                     VARCHAR(64),
    file_storage_key              VARCHAR(500),
    total_rows                    INTEGER,
    valid_rows                    INTEGER,
    invalid_rows                  INTEGER,
    processed_rows                INTEGER,
    successful_rows               INTEGER,
    failed_rows                   INTEGER,
    current_batch                 INTEGER,
    total_batches                 INTEGER,
    summary_storage_key           VARCHAR(500),
    validation_started_at         TIMESTAMP,
    validation_completed_at       TIMESTAMP,
    processing_started_at         TIMESTAMP,
    processing_completed_at       TIMESTAMP,
    cancelled_at                  TIMESTAMP,
    retry_count                   INTEGER,
    max_retry_count               INTEGER,
    last_retry_at                 TIMESTAMP,
    timeout_at                    TIMESTAMP,
    error_message                 TEXT,
    meta_data                     JSONB,
    working_file_storage_key      VARCHAR(500),
    validation_errors_storage_key VARCHAR(500),
    created_by                    VARCHAR(255),
    created_at                    TIMESTAMP,
    updated_by                    VARCHAR(255),
    updated_at                    TIMESTAMP,
    version                       BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: displayconfig
-- =============================================================================

-- Entity: AppDisplayConfig
CREATE TABLE n_app_display_config (
    id        BIGSERIAL       PRIMARY KEY,
    app_type  VARCHAR(50)     NOT NULL UNIQUE,
    config    JSONB           NOT NULL,
    is_active BOOLEAN         NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- =============================================================================
-- MODULE: integrations
-- =============================================================================

-- Entity: ThirdPartyServiceConfig
CREATE TABLE n_third_party_service_config (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(255),
    service         VARCHAR(255),
    primary_config  BIGINT          NOT NULL,
    fallback_config BIGINT,
    retry_count     INTEGER,
    is_primary      BOOLEAN,
    is_active       BOOLEAN,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,
    version         BIGINT          DEFAULT 0
);

-- Entity: ThirdPartyProviderConfig
CREATE TABLE n_third_party_provider_config (
    id       BIGSERIAL       PRIMARY KEY,
    name     VARCHAR(255),
    provider VARCHAR(255),
    configs  VARCHAR(255),
    active   BOOLEAN,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version    BIGINT          DEFAULT 0
);

-- Entity: ThirdPartyResponseLog
CREATE TABLE n_third_party_response_log (
    id               BIGSERIAL       PRIMARY KEY,
    entity_type      VARCHAR(3)      NOT NULL,
    entity_id        BIGINT,
    request_method   VARCHAR(16)     NOT NULL,
    url              VARCHAR(512)    NOT NULL,
    request          VARCHAR(255),
    response         VARCHAR(255),
    http_status_code INTEGER,
    response_time_ms BIGINT,
    provider_name    VARCHAR(50),
    provider_ref_id  VARCHAR(50),
    business_purpose VARCHAR(255),
    created_by       VARCHAR(255),
    created_at       TIMESTAMP,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP,
    version          BIGINT          DEFAULT 0
);

-- Entity: WhatsAppLog
CREATE TABLE whatsapp_logs (
    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id    VARCHAR(100)    NOT NULL UNIQUE,
    phone_number  VARCHAR(20)     NOT NULL,
    template_name VARCHAR(100),
    broadcast_name VARCHAR(100),
    status        VARCHAR(20)     NOT NULL,
    sent_time     TIMESTAMP,
    delivered_time TIMESTAMP,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP,
    version       BIGINT          DEFAULT 0
);


-- =============================================================================
-- =============================================================================
-- JSONB COLUMN STRUCTURES
-- =============================================================================
-- =============================================================================

-- =============================================================================
-- Table: n_lead
-- =============================================================================

-- Column: contacts → List<Long>
-- Column: co_applicants → List<Long>
-- Column: notes → List<Long>
-- Column: external_ids → Map<String, String>

-- Column: workflow_details (WorkflowDetails)
--   {
--     "workflowConfigKey": "String",
--     "currentStageDetails": {
--       "stageKey": "String", "subStageKey": "String", "assignedTo": "String",
--       "assignedAt": "LocalDateTime", "enteredAt": "LocalDateTime"
--     },
--     "lastStageDetails": {
--       "stageKey": "String", "subStageKey": "String", "assignedTo": "String",
--       "assignedAt": "LocalDateTime", "enteredAt": "LocalDateTime",
--       "exitedAt": "LocalDateTime", "movedBy": "String", "remarks": "String"
--     }
--   }

-- Column: preliminary_details (PreliminaryDetails)
--   { "isWhatsAppDIYFormCompleted": "Boolean", "whatsAppDIYForm": "Map<String, String>", "monthlyFamilyIncome": "BigDecimal" }

-- Column: document_details (List<DocumentDetail>)
--   [{ "id": "Long", "tag": "List<String>" }]

-- Column: credit_rating_details (CreditRatingDetails)
--   {
--     "underwriter": "String", "occupationProfile": "String", "roofProfile": "String",
--     "ltv": "String", "foir": "String", "monthlyFamilyIncome": "String",
--     "propertyDocumentType": "String", "eligibleLoanAmount": "BigDecimal",
--     "location": "String", "bureauRating": "String", "customerProfiles": "String"
--   }

-- Column: disbursement_details (DisbursementDetails)
--   {
--     "disbursedAmount": "BigDecimal", "roi": "BigDecimal", "tenureValue": "Integer",
--     "tenureType": "TenureType (enum)", "disbursedDate": "LocalDate", "processingFees": "BigDecimal",
--     "tranches": [{ "identifier": "UUID", "amount": "BigDecimal", "date": "LocalDate" }]
--   }

-- Column: reasons (ReasonDetails)
--   { "reject": "String", "withdrawn": "String", "onhold": "String", "dropoff": "String" }

-- Column: proposed_details (ProposedDetails)
--   { "proposedLoanAmount": "BigDecimal", "roi": "BigDecimal", "tenureValue": "Integer", "tenureType": "TenureType (enum)", "emi": "BigDecimal" }

-- Column: other_details (OtherDetails)
--   {
--     "primaryContactId": "Long", "lastCallId": "Long",
--     "propertyDetails": {
--       "address": { "id": "String", "addressType": "AddressType", "address": "String", "pincode": "String",
--                    "district": "String", "country": "String", "state": "String", "taluka": "String",
--                    "districtCode": "String", "stateCode": "String", "countryCode": "String", "talukaCode": "String",
--                    "districtId": "Long", "stateId": "Long", "countryId": "Long", "talukaId": "Long",
--                    "villageCode": "String", "villageId": "Long", "villageName": "String", "isServiceable": "Boolean" },
--       "geoData": { "latitude": "Double", "longitude": "Double" },
--       "propertyType": "String", "propertyConstructionStage": "String",
--       "owner": "String", "ownerRelation": "String",
--       "propertyMeasurementDetails": { "buildUpArea": "String", "siteArea": "String" },
--       "documentChecklist": { "ekhataType": "String", "ekhataStatus": "String", "saleDeed": "String",
--                              "propertyTax": "String", "statementOfAccounts": "String", "otherDocs": "String" }
--     },
--     "priority": "String", "intent": "String", "preferredCallStartTime": "LocalTime", "preferredCallEndTime": "LocalTime",
--     "noOfCampaignCalls": "Long", "currentCustomerFormStep": "String"
--   }

-- Column: onhold_details (OnHoldDetails)
--   { "onHoldMovementDate": "LocalDateTime", "onHoldBy": "String", "holdFollowUpDate": "LocalDate" }

-- Column: rejection_details (RejectionDetails)
--   { "rejectionDate": "LocalDateTime", "rejectedBy": "String" }

-- Column: withdrawn_details (WithdrawnDetails)
--   { "withdrawnDate": "LocalDateTime", "withdrawnBy": "String" }

-- Column: dropoff_details (DropoffDetails)
--   { "dropoffDate": "LocalDateTime", "dropoffBy": "String" }

-- Column: income_obligation_details (IncomeObligationDetails)
--   {
--     "incomeDetails": [{ "incomeSource": "String", "amount": "BigDecimal",
--                         "documentChecklist": [{ "documentType": "String", "status": "String" }] }],
--     "obligationDetails": { "existingEmi": "BigDecimal" },
--     "monthlyFamilyIncome": "BigDecimal"
--   }

-- Column: call_summary_details (CallSummaryDetails)
--   {
--     "totalOutboundCalls": "Integer", "outboundConnectedCalls": "Integer",
--     "bestTimeToCall": { "start": "LocalTime", "end": "LocalTime" },
--     "lastConnectedCallAt": "LocalDateTime", "lastCallAttemptAt": "LocalDateTime",
--     "averageOutboundTalkDurationSeconds": "Double", "consecutiveNoAnswers": "Integer"
--   }

-- Column: task_timeline (TaskTimeline)
--   {
--     "previous": { "taskIdentifier": "UUID", "dueAt": "LocalDateTime", "taskConfigKey": "String", "taskName": "String" },
--     "next":     { "taskIdentifier": "UUID", "dueAt": "LocalDateTime", "taskConfigKey": "String", "taskName": "String" }
--   }

-- Column: bre_executions (BREExecutions)
--   { "eligibility": { "status": "String", "resultIdentifier": "UUID" } }

-- =============================================================================
-- Table: n_contact
-- =============================================================================

-- Column: cb_enquiry_id → List<Long>

-- =============================================================================
-- Table: n_lead_lender
-- =============================================================================

-- Column: login_details (LoginDetails)
--   { "loginId": "String", "loginDate": "LocalDate", "loginFees": "BigDecimal", "lenderRemarks": "String" }

-- Column: rm_details (RmDetails)
--   { "name": "String", "mobileNumber": "String" }

-- Column: approved_details (ApprovedDetails)
--   { "approvedAmount": "BigDecimal", "roi": "BigDecimal", "emi": "BigDecimal",
--     "tenureValue": "Integer", "tenureType": "TenureType", "approvedDate": "LocalDate",
--     "processingFees": "BigDecimal", "sanctionExpiry": "LocalDate", "insuranceFees": "BigDecimal", "otherFees": "BigDecimal" }

-- Column: rejection_details (RejectionDetails)
--   { "rejectedBy": "String", "rejectionReason": "String", "remarks": "String", "rejectionDate": "LocalDateTime" }

-- =============================================================================
-- Table: n_lead_activity / n_advisor_activity
-- =============================================================================

-- Column: metadata → Map<String, Object>

-- =============================================================================
-- Table: n_person
-- =============================================================================

-- Column: mobile_numbers (List<MobileNumberDetails>)
--   [{ "number": "String", "isPrimary": "Boolean", "isWhatsappAvailable": "Boolean" }]

-- Column: address (List<AddressData>)
--   [{ "id": "String", "addressType": "AddressType", "address": "String", "pincode": "String",
--      "district": "String", "country": "String", "state": "String", "taluka": "String",
--      "districtCode": "String", "stateCode": "String", "countryCode": "String", "talukaCode": "String",
--      "districtId": "Long", "stateId": "Long", "countryId": "Long", "talukaId": "Long",
--      "villageCode": "String", "villageId": "Long", "villageName": "String", "isServiceable": "Boolean" }]

-- Column: identifiers (List<IdentifierData>)
--   [{ "id": "UUID", "type": "IdentifierType (enum)", "identifier": "String" }]

-- Column: data_ext → Map<String, Object>
-- Column: cb_enquiry_id → List<Long>

-- Column: cb_details (CreditBureauDetails)
--   { "latestSuccessEnquiryId": "Long", "latestEnquiryId": "Long" }

-- Column: consent_details (List<ConsentInfo>)
--   [{ "id": "Long", "type": "String" }]

-- =============================================================================
-- Table: n_advisor
-- =============================================================================

-- Column: remarks (AdvisorRemarks)
--   { "rejected": "String", "dormant": "String", "outOfGeo": "String" }

-- Column: bank_details (List<BankDetails>)
--   [{ "bankIdentifier": "UUID", "isPrimary": "Boolean", "nameAsPerPassbook": "String",
--      "accountNo": "String", "bankName": "String", "ifscCode": "String",
--      "upid": [{ "upid": "String" }], "status": "BankDetailsStatus (enum)" }]

-- Column: qualification_details (QualificationDetails)
--   { "highestQualification": "String" }

-- Column: other_details (OtherDetails)
--   { "occupationType": "String", "occupation": "String", "lastCallId": "Long",
--     "preferredCallStartTime": "LocalTime", "preferredCallEndTime": "LocalTime" }

-- Column: segmentation_details (SegmentationDetails)
--   { "segmentation": "String" }

-- Column: rejection_details (RejectionDetails)
--   { "rejectionDate": "LocalDateTime", "rejectedBy": "String" }

-- Column: notes → List<Long>
-- Column: external_ids → Map<String, String>
-- Column: call_logs (List<CallLogDetails>) → [{ "callLogId": "Long" }]

-- =============================================================================
-- Table: n_tasks
-- =============================================================================

-- Column: outcome_details (OutcomeDetails)
--   { "remarks": "String", "completedAt": "LocalDateTime", "completedBy": "String",
--     "rescheduleReasonCodeValueKey": "String", "locationDetails": "Map<String, Object>" }

-- Column: task_details (TaskDetails)
--   { "entityId": "UUID", "entityType": "EntityType (enum)", "stageKey": "String",
--     "preferredCallWindow": { "start": "LocalDateTime", "end": "LocalDateTime" },
--     "creatorRemarks": "String", "iterationCount": "Integer",
--     "rescheduledFromTaskIdentifier": "UUID", "rescheduleReasonCodeValueKey": "String",
--     "rescheduledFromTaskRemarks": "String" }

-- =============================================================================
-- Table: n_task_config
-- =============================================================================

-- Column: task_config_details (TaskConfigDetails)
--   { "allowedOutcomesCodeValueKey": "String", "isRescheduledAllowed": "Boolean",
--     "rescheduleReasonsCodeValueKey": "String", "allowedRoles": "List<String>",
--     "dueDateLogicExpression": "String", "isAdhocTaskAllowed": "Boolean" }

-- =============================================================================
-- Table: n_workflow_config
-- =============================================================================

-- Column: workflow_config_details (WorkflowConfigDetails)
--   {
--     "landingStage": "String",
--     "stages": [{
--       "stageKey": "String", "allowedAdhocTasks": "List<String>",
--       "stageTasks": [{ "fromStage": "String", "taskConfigKeys": "List<String>" }],
--       "defaultSubStage": "String",
--       "taskCompletionRules": [{ "taskConfigKey": "String", "breRuleUname": "String" }]
--     }]
--   }

-- =============================================================================
-- Table: n_pending_workflow_actions
-- =============================================================================

-- Column: action_details (ActionDetails)
--   { "type": "String", "taskConfigKey": "String", "targetStageKey": "String",
--     "targetSubStageKey": "String", "assignTo": "String", "outcome": "String", "dueDate": "LocalDateTime" }

-- =============================================================================
-- Table: n_stage_config
-- =============================================================================

-- Column: stage_config (StageConfigDetails)
--   { "possibleNextStages": [{ "stageKey": "String", "allowedRoles": "List<String>" }],
--     "externalDisplayName": "String", "externalOrder": "Integer" }

-- Column: assignee_roles (AssigneeRoles)
--   { "roles": "List<String>" }

-- =============================================================================
-- Table: n_bre_configs
-- =============================================================================

-- Column: config (Configs)
--   { "dataProviderId": "Long", "provider": "BREProvider (enum)",
--     "goRulesProviderDetails": { "ruleJsonFileId": "Long" } }

-- =============================================================================
-- Table: n_user
-- =============================================================================

-- Column: preferences → Map<String, Object>

-- =============================================================================
-- Table: n_consent
-- =============================================================================

-- Column: consent_sent_details → { "consentSentTime": "LocalDateTime" }
-- Column: consent_received_details → { "consentReceivedTime": "LocalDateTime", "auditId": "String" }
-- Column: consent_withdrawn_details → { "consentWithdrawalRequestedTime": "LocalDateTime", "auditId": "String" }

-- =============================================================================
-- Table: n_document
-- =============================================================================

-- Column: data_ext → Map<String, Object>

-- =============================================================================
-- Table: n_office
-- =============================================================================

-- Column: address_data (AddressData)
--   { "id": "String", "addressType": "AddressType", "address": "String", "pincode": "String",
--     "district": "String", "country": "String", "state": "String", "taluka": "String",
--     "districtCode": "String", "stateCode": "String", "countryCode": "String", "talukaCode": "String",
--     "districtId": "Long", "stateId": "Long", "countryId": "Long", "talukaId": "Long",
--     "villageCode": "String", "villageId": "Long", "villageName": "String", "isServiceable": "Boolean" }

-- =============================================================================
-- Table: n_lender_office
-- =============================================================================

-- Column: address (AddressDetails)
--   { "address": { <same AddressData structure as n_office.address_data> } }

-- =============================================================================
-- Table: n_call_log
-- =============================================================================

-- Column: recording_details (RecordingDetails)
--   { "url": "String" }

-- Column: completion_details (CompletionDetails)
--   { "duration": "Long", "startTime": "LocalDateTime", "endTime": "LocalDateTime",
--     "legs": [{ "duration": "String", "status": "CallStatus (enum)", "direction": "String" }] }

-- Column: ai_analysis (AiAnalysisDetails)
--   { "jobId": "String", "status": "AtlasJobStatus (enum)", "summaryUrl": "String",
--     "analysisUrl": "String", "transcriptUrl": "String", "error": "String" }

-- =============================================================================
-- Table: n_reconciliation_log
-- =============================================================================

-- Column: changes → Map<String, Object>

-- =============================================================================
-- Table: n_campaign
-- =============================================================================

-- Column: provider_details (ProviderDetails)
--   { "voiceDetails": { "callerId": "String", "appFlowId": "String", "noOfRetries": "Integer",
--     "retryInterval": "Integer", "cpm": "Integer", "listId": "String", "documentUploadId": "String",
--     "documentStatus": "CampaignProviderDocumentStatus (enum)", "scheduledAt": "LocalDateTime" },
--     "error": "String" }

-- Column: document_details (DocumentDetails)
--   { "documentStatus": "CampaignDocumentStatus (enum)", "documentId": "Long", "error": "String" }

-- Column: summary (Summary)
--   { "reportUrl": "String", "scheduled": "Long", "initialized": "Long",
--     "completed": "Long", "failed": "Long", "inProgress": "Long" }

-- =============================================================================
-- Table: n_campaign_config
-- =============================================================================

-- Column: config (Configs)
--   { "dataProviderId": "Long", "fileType": "FileType (enum)", "campaignType": "CampaignType (enum)",
--     "voiceConfigs": { "callerId": "String", "appFlowId": "String", "appFlowName": "String",
--     "defaultNoOfRetries": "Integer", "defaultRetryInterval": "Integer", "defaultCpm": "Integer" } }

-- =============================================================================
-- Table: n_sourcing_channel_details
-- =============================================================================

-- Column: marketing_details (MarketingDetails)
--   { "sourceId": "String", "campaignId": "String", "sourceUrl": "String", "referredByCode": "String" }

-- =============================================================================
-- Table: n_master_code / n_master_code_value / n_product
-- =============================================================================

-- Column: name / value / description (MasterLanguageData)
--   { "default": "String", "kn": "String" }

-- Column: icons (IconsData) — on n_master_code_value only
--   { "default": { "small": { "url": "String", "documentId": "Long" }, "medium": {...}, "large": {...},
--     "xl": {...}, "xxl": {...}, "svg": {...} }, "crm": {...}, "web": {...}, "app": {...} }

-- =============================================================================
-- Table: n_cb_summary
-- =============================================================================

-- Column: score_factor_details (List<ScoreFactor>)
--   [{ "factorType": "String", "factorDescription": "String" }]

-- =============================================================================
-- Unstructured JSONB columns (Map<String, Object> / dynamic)
-- =============================================================================

-- n_notification_config.config → Map<String, Object>
-- n_notification_template.variables → Map<String, Object>
-- n_notification_template.buttons → List<Map<String, Object>>
-- n_notification_record.notification_payload → Map<String, Object>
-- n_notification_record.details → Map<String, Object>
-- n_notification_record.error_json → Map<String, Object>
-- n_notification_receipt.message_payload → Map<String, Object>
-- n_notification_receipt.details → Map<String, Object>
-- n_notification_receipt.schedules → List<Map<String, Object>>
-- n_notification_receipt.remarks → Map<String, Object>
-- n_notification_receipt.error_json → Map<String, Object>
-- n_lead_whatsapp_notification.status_track → Map<String, Object>
-- n_advisor_whatsapp_notification.status_track → Map<String, Object>
-- n_bulk_operation.meta_data → Map<String, Object>
-- n_app_display_config.config → Map<String, Object>
-- n_master_district.value → JSONB
-- n_master_taluka.value → JSONB
-- n_master_village.value → JSONB
