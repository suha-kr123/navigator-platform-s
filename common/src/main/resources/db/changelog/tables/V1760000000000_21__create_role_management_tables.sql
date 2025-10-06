CREATE TABLE roles
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name       varchar(100) NOT NULL,
    created_by varchar(255),
    created_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    version    bigint           DEFAULT 0,
    CONSTRAINT uq_roles_name UNIQUE (name)
);

CREATE TABLE permissions
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name       varchar(100) NOT NULL,
    action     varchar(40),
    operation  varchar(40),
    module     varchar(40),
    created_by varchar(255),
    created_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    version    bigint           DEFAULT 0,
    CONSTRAINT uq_permissions_name UNIQUE (name)
);

CREATE TABLE permission_group
(
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name       varchar(100) NOT NULL,
    created_by varchar(255),
    created_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(255),
    updated_at timestamp        DEFAULT CURRENT_TIMESTAMP,
    version    bigint           DEFAULT 0,
    CONSTRAINT uq_permission_group_name UNIQUE (name)
);

CREATE TABLE permission_group_mapping
(
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_id       uuid NOT NULL,
    permission_group_id uuid NOT NULL,
    created_by          varchar(255),
    created_at          timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by          varchar(255),
    updated_at          timestamp        DEFAULT CURRENT_TIMESTAMP,
    version             bigint           DEFAULT 0,
    CONSTRAINT fk_permission_group_mapping_permission FOREIGN KEY (permission_id) REFERENCES permissions (id),
    CONSTRAINT fk_permission_group_mapping_permission_group FOREIGN KEY (permission_group_id) REFERENCES permission_group (id),
    CONSTRAINT uq_permission_group_mapping UNIQUE (permission_id, permission_group_id)
);


CREATE TABLE role_permission_group_mapping
(
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id             uuid NOT NULL,
    permission_group_id uuid NOT NULL,
    created_by          varchar(255),
    created_at          timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by          varchar(255),
    updated_at          timestamp        DEFAULT CURRENT_TIMESTAMP,
    version             bigint           DEFAULT 0,
    CONSTRAINT fk_role_permission_group_mapping_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permission_group_mapping_permission_group FOREIGN KEY (permission_group_id) REFERENCES permission_group (id),
    CONSTRAINT uq_role_permission_group_mapping UNIQUE (role_id, permission_group_id)
);

CREATE INDEX idx_role_permission_group_mapping_role_id ON role_permission_group_mapping(role_id);
CREATE INDEX idx_role_permission_group_mapping_group_id ON role_permission_group_mapping(permission_group_id);

CREATE TABLE role_permission_mapping
(
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id       uuid NOT NULL,
    permission_id uuid NOT NULL,
    created_by    varchar(255),
    created_at    timestamp        DEFAULT CURRENT_TIMESTAMP,
    updated_by    varchar(255),
    updated_at    timestamp        DEFAULT CURRENT_TIMESTAMP,
    version       bigint           DEFAULT 0,
    CONSTRAINT fk_role_permission_mapping_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permission_mapping_permission FOREIGN KEY (permission_id) REFERENCES permissions (id),
    CONSTRAINT uq_role_permission_mapping UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permission_mapping_role_id ON role_permission_mapping(role_id);
CREATE INDEX idx_role_permission_mapping_permission_id ON role_permission_mapping(permission_id);


