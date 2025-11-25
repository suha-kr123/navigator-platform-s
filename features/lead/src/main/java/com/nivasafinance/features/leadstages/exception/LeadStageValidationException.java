package com.nivasafinance.features.leadstages.exception;

import com.nivasafinance.common.exception.ValidationException;

/**
 * Custom validation exception for lead stage operations.
 * Extends ValidationException to maintain consistency with global exception handling.
 */
public class LeadStageValidationException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public LeadStageValidationException(String message) {
        super(message);
    }

    public static LeadStageValidationException nullLeadId() {
        return new LeadStageValidationException("leadId cannot be null");
    }

    public static LeadStageValidationException nullRequest() {
        return new LeadStageValidationException("request cannot be null");
    }

    public static LeadStageValidationException nullOrEmptyStageKey() {
        return new LeadStageValidationException("stageKey cannot be null or empty");
    }

    public static LeadStageValidationException nullOrEmptySubStageKey() {
        return new LeadStageValidationException("subStageKey cannot be null or empty");
    }

    public static LeadStageValidationException nullOrEmptyAssignedTo() {
        return new LeadStageValidationException("newAssignedTo cannot be null or empty");
    }

    public static LeadStageValidationException nullOrEmptyLeadIdentifiers() {
        return new LeadStageValidationException("leadIdentifiers cannot be null or empty");
    }
}

