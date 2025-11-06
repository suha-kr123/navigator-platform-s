package com.nivasafinance.features.advisorlead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class AdvisorLeadMappingNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AdvisorLeadMappingNotFoundException(UUID mappingId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.advisor.lead.mapping.not.found",
                new Object[]{mappingId.toString()},
                messageSource
        ));
    }
}

