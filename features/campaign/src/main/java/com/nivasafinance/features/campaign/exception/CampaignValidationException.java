package com.nivasafinance.features.campaign.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CampaignValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CampaignValidationException(String message) {
        super(message);
    }

    public CampaignValidationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}

