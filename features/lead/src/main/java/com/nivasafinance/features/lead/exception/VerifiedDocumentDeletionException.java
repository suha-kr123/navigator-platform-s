package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class VerifiedDocumentDeletionException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 8347562918374650123L;

    public VerifiedDocumentDeletionException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}

