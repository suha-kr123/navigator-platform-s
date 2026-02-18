package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public final class LeadExceptionFactory {

    private LeadExceptionFactory() {
        // Utility class
    }

    public static BadRequestException cbDataIncomplete(String missingFields, MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage("error.lead.cb.data.incomplete", new Object[]{missingFields}, messageSource));
    }

    public static LeadNotFoundException leadNotFoundByReferralTrackingCode(String referralTrackingCode, MessageSource messageSource) {
        return new LeadNotFoundException(referralTrackingCode, messageSource);
    }

    public static LeadOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new LeadOperationException("error.lead.retrieve.entity.failed", messageSource);
    }
}
