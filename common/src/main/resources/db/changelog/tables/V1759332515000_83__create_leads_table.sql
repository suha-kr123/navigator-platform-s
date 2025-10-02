CREATE TABLE leads
(
    id               uuid PRIMARY KEY default gen_random_uuid(),
    requested_amount jsonb,
    purpose          varchar(40),
    product_code     varchar(255),
    pipeline_key     varchar(255),
    current_stage    varchar(255),
    sourcing_channel        varchar(255),
    preliminary_information jsonb,
    ext_data                jsonb,
    task_data               jsonb,
    address_id       uuid,
    document_ids            jsonb,
    note_ids                jsonb,
    stage_ids               jsonb,
    person_data             jsonb,
    created_by              varchar(255),
    created_at              timestamp,
    updated_by              varchar(255),
    updated_at              timestamp,
    version                 bigint
);
