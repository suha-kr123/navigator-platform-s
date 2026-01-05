package com.nivasafinance.features.campaign.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CampaignConfigOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -19094762348956215L;

    public CampaignConfigOperationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}

