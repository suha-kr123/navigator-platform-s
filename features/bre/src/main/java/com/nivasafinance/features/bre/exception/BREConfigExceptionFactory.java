package com.nivasafinance.features.bre.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public final class BREConfigExceptionFactory {

    private BREConfigExceptionFactory() {
    }

    public static BREConfigOperationException saveFailed(MessageSource messageSource) {
        return new BREConfigOperationException("error.bre.config.save.failed", null, messageSource);
    }

    public static BREConfigOperationException retrieveByIdFailed(Long id, MessageSource messageSource) {
        return new BREConfigOperationException("error.bre.config.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static BREConfigOperationException retrieveByUnameFailed(String uname, MessageSource messageSource) {
        return new BREConfigOperationException("error.bre.config.retrieve.by.uname.failed", new Object[]{uname}, messageSource);
    }

    public static BREConfigOperationException retrieveAllFailed(MessageSource messageSource) {
        return new BREConfigOperationException("error.bre.config.retrieve.all.failed", null, messageSource);
    }

    public static BREConfigNotFoundException notFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.bre.config.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new BREConfigNotFoundException(message);
    }

    public static BREConfigNotFoundException notFoundByUname(String uname, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.bre.config.not.found.by.uname",
                new Object[]{uname},
                messageSource
        );
        return new BREConfigNotFoundException(message);
    }

    public static BREConfigOperationException logSaveFailed(MessageSource messageSource) {
        return new BREConfigOperationException("error.bre.log.save.failed", null, messageSource);
    }

    public static BREConfigOperationException logRetrieveByIdFailed(Long id, MessageSource messageSource) {
        return new BREConfigOperationException("error.bre.log.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static BREConfigNotFoundException logNotFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.bre.log.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new BREConfigNotFoundException(message);
    }
}
