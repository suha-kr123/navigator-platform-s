CREATE TABLE documents
(
    document_id         uuid PRIMARY KEY,
    document_type       varchar(255) NOT NULL,
    verification_status varchar(255) NOT NULL,
    verification_notes  varchar(500),
    file_name           varchar(255) NOT NULL,
    file_type           varchar(255),
    file_size           bigint,
    provider            varchar(255) NOT NULL,
    storage_key         varchar(255) NOT NULL,
    file_url            varchar(255),
    category            varchar(255),
    doc_type            varchar(255),
    tags                jsonb,
    data_ext            jsonb,
    created_by          varchar(255),
    created_at          timestamp,
    updated_by          varchar(255),
    updated_at          timestamp,
    version             bigint
);
