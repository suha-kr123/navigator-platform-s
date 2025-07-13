CREATE TABLE person
(
    id         BIGINT PRIMARY KEY,
    profile_id VARCHAR(255) not null,
    details    JSONB        NOT NULL
);

CREATE INDEX idx_person_profile_id ON person (profile_id);
CREATE INDEX idx_person_name ON person USING GIN((details->'name'));
CREATE INDEX idx_person_phone ON person USING GIN((details->'phoneNo'));
CREATE INDEX idx_person_identifiers_id_gin
    ON person
    USING gin (
    (jsonb_path_query_array(details, '$.identifiers[*].id'))
    );