-- Create documents table
CREATE TABLE documents (
    document_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Core file metadata
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    
    -- Storage information
    provider VARCHAR(50) NOT NULL CHECK (provider IN ('AWS_S3', 'LOCAL')),
    storage_key VARCHAR(500) NOT NULL,
    file_url VARCHAR(1000),
    
    -- Classification
    category VARCHAR(100),
    doc_type VARCHAR(100),
    
    -- JSONB fields for flexible metadata
    tags JSONB,
    ext_data JSONB,
    
    -- Audit fields (inherited from AuditableEntity)
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create audit triggers for documents table
CREATE TRIGGER tr_documents_audit_insert
    BEFORE INSERT ON documents
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_documents_audit_update
    BEFORE UPDATE ON documents
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();


