package com.nivasafinance.features.lender.lender.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.UUID;

public class LenderExceptionFactory {

    public static LenderOperationException createFailed(MessageSource messageSource) {
        return new LenderOperationException("error.lender.operation.create", messageSource);
    }

    public static LenderOperationException retrieveFailed(MessageSource messageSource) {
        return new LenderOperationException("error.lender.operation.retrieve", messageSource);
    }

    public static LenderOperationException duplicateKey(String key, MessageSource messageSource) {
        return new LenderOperationException(
                ExceptionUtils.createLocalizedMessage("error.lender.duplicate.key", new Object[]{key}, messageSource));
    }

    public static LenderNotFoundException lenderNotFound(UUID lenderId, MessageSource messageSource) {
        return new LenderNotFoundException(lenderId, messageSource);
    }

    public static LenderKeyNotFoundException lenderNotFound(String key, MessageSource messageSource) {
        return new LenderKeyNotFoundException(key, messageSource);
    }
}

