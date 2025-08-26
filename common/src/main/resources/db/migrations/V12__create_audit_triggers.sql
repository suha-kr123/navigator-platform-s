-- Function to handle audit fields on INSERT
CREATE OR REPLACE FUNCTION set_audit_fields_on_insert()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = COALESCE(NEW.created_at, CURRENT_TIMESTAMP);
    NEW.updated_at = COALESCE(NEW.updated_at, CURRENT_TIMESTAMP);
    NEW.version = COALESCE(NEW.version, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to handle audit fields on UPDATE
CREATE OR REPLACE FUNCTION set_audit_fields_on_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for person table
CREATE TRIGGER tr_person_audit_insert
    BEFORE INSERT ON person
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_person_audit_update
    BEFORE UPDATE ON person
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for master_pincode table
CREATE TRIGGER tr_master_pincode_audit_insert
    BEFORE INSERT ON master_pincode
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_master_pincode_audit_update
    BEFORE UPDATE ON master_pincode
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for address table
CREATE TRIGGER tr_address_audit_insert
    BEFORE INSERT ON address
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_address_audit_update
    BEFORE UPDATE ON address
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for person_address_mapping table
CREATE TRIGGER tr_person_address_mapping_audit_insert
    BEFORE INSERT ON person_address_mapping
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_person_address_mapping_audit_update
    BEFORE UPDATE ON person_address_mapping
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for person_identifiers table
CREATE TRIGGER tr_person_identifiers_audit_insert
    BEFORE INSERT ON person_identifiers
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_person_identifiers_audit_update
    BEFORE UPDATE ON person_identifiers
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for advisor table
CREATE TRIGGER tr_advisor_audit_insert
    BEFORE INSERT ON advisor
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_advisor_audit_update
    BEFORE UPDATE ON advisor
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for leads table
CREATE TRIGGER tr_leads_audit_insert
    BEFORE INSERT ON leads
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_leads_audit_update
    BEFORE UPDATE ON leads
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for lead_applicant table
CREATE TRIGGER tr_lead_applicant_audit_insert
    BEFORE INSERT ON lead_applicant
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_lead_applicant_audit_update
    BEFORE UPDATE ON lead_applicant
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for payments table
CREATE TRIGGER tr_payments_audit_insert
    BEFORE INSERT ON payments
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_payments_audit_update
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create triggers for advisor_lead_mapping table
CREATE TRIGGER tr_advisor_lead_mapping_audit_insert
    BEFORE INSERT ON advisor_lead_mapping
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_advisor_lead_mapping_audit_update
    BEFORE UPDATE ON advisor_lead_mapping
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();
