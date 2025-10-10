CREATE TABLE notes
(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    title      varchar(255) NOT NULL,
    content    text         NOT NULL,
    created_by varchar(255),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    version    bigint DEFAULT 0
);
