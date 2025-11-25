package com.nivasafinance.features.workflow.constants;

public final class WorkflowConstants {

    private WorkflowConstants() {
    }

    public static final class ContextKeys {
        private ContextKeys() {
        }

        public static final String ASSIGNED_TO = "assignedTo";
        public static final String DUE_AT = "dueAt";
    }

    public static final class TaskDetails {
        private TaskDetails() {
        }

        public static final String STAGE_KEY = "stage_key";
        public static final String CREATOR_REMARKS_STAGE_PREFIX = "Stage: ";
    }

    public static final class StageTask {
        private StageTask() {
        }

        public static final String LANDING_STAGE = "landing";
        public static final String DEFAULT_STAGE = "default";
    }

    public static final class Workflow {
        private Workflow() {
        }

        public static final String DEFAULT_WORKFLOW_KEY = "DEFAULT_LEAD_WORKFLOW";
    }

    public static final class EventMapping {
        private EventMapping() {
        }

        public static final String DETAILS_KEY_EVENT_FILTERS = "eventFilters";
        public static final String CONTEXT_KEY_EVENT_NAME = "eventName";
        public static final String CONTEXT_KEY_EVENT_DETAILS = "eventDetails";
        public static final String WORKFLOW_CONFIG_KEY = "workflowConfig";
        public static final String EVENT_DETAILS_KEY_WORKFLOW_CONFIG_KEY = "workflowConfigKey";
        
        // Event Names
        public static final String EVENT_NAME_STAGE_TRANSITION = "STAGE_TRANSITIONED";
        
        // Event Details Keys
        public static final String DETAILS_KEY_PRODUCT_CODE = "productCode";
        public static final String DETAILS_KEY_OFFICE_KEY = "officeKey";
        public static final String DETAILS_KEY_STATUS = "status";
        
        // Context Keys
        public static final String CONTEXT_KEY_LEAD_ID = "leadId";
        public static final String CONTEXT_KEY_LEAD_IDENTIFIER = "leadIdentifier";
    }

    public static final class WorkflowConfigDetails {
        private WorkflowConfigDetails() {
        }

        public static final String LANDING_STAGE = "LANDING_STAGE";
        public static final String DEFAULT_STAGE = "DEFAULT";
    }

}
