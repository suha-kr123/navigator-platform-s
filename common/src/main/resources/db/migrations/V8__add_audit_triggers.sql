-- Combined function to set created_at on insert and always update updated_at
CREATE OR REPLACE FUNCTION set_audit_timestamps()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.created_at IS NULL THEN
            NEW.created_at = CURRENT_TIMESTAMP;
        END IF;
    END IF;
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach to leads table
CREATE TRIGGER leads_audit_timestamps
BEFORE INSERT OR UPDATE ON leads
FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();

-- Attach to lead_applicant table
CREATE TRIGGER lead_applicant_audit_timestamps
BEFORE INSERT OR UPDATE ON lead_applicant
FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();

-- Attach to person table
CREATE TRIGGER person_audit_timestamps
BEFORE INSERT OR UPDATE ON person
FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();

--Attach to advisor table
CREATE TRIGGER advisor_audit_timestamps
BEFORE INSERT OR UPDATE ON advisor
FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();

--Attach to pincode table
CREATE TRIGGER pincode_audit_timestamps
BEFORE INSERT OR UPDATE ON master_pincode
FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();