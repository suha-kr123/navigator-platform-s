-- Create audit triggers for pipelines table
CREATE TRIGGER tr_pipelines_audit_insert
    BEFORE INSERT ON pipelines
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_pipelines_audit_update
    BEFORE UPDATE ON pipelines
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create audit triggers for stages table
CREATE TRIGGER tr_stages_audit_insert
    BEFORE INSERT ON stages
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_stages_audit_update
    BEFORE UPDATE ON stages
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create audit triggers for transition_definitions table
CREATE TRIGGER tr_transition_definitions_audit_insert
    BEFORE INSERT ON transition_definitions
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_transition_definitions_audit_update
    BEFORE UPDATE ON transition_definitions
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create audit triggers for task_definitions table
CREATE TRIGGER tr_task_definitions_audit_insert
    BEFORE INSERT ON task_definitions
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_task_definitions_audit_update
    BEFORE UPDATE ON task_definitions
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();

-- Create audit triggers for lead_tasks table
CREATE TRIGGER tr_lead_tasks_audit_insert
    BEFORE INSERT ON lead_tasks
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_insert();

CREATE TRIGGER tr_lead_tasks_audit_update
    BEFORE UPDATE ON lead_tasks
    FOR EACH ROW
    EXECUTE FUNCTION set_audit_fields_on_update();
