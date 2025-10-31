package com.nivasafinance.common.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;

/**
 * Utility class for common exception handling patterns
 */
public class ExceptionUtils {

    /**
     * Creates a localized message using MessageSource
     */
    public static String createLocalizedMessage(
            String messageKey,
            Object[] args,
            MessageSource messageSource) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageKey, args, locale);
    }

    /**
     * Validates that a required field is not null
     */
    public static void requireNotNull(
            Object value,
            String fieldName,
            String messageKey,
            MessageSource messageSource) {
        if (value == null) {
            throw new ValidationException(
                    createLocalizedMessage(messageKey, new Object[]{fieldName}, messageSource));
        }
    }

    /**
     * Validates that a required field is not blank (for strings)
     */
    public static void requireNotBlank(
            String value,
            String fieldName,
            String messageKey,
            MessageSource messageSource) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(
                    createLocalizedMessage(messageKey, new Object[]{fieldName}, messageSource));
        }
    }

    /**
     * Validates that a condition is true
     */
    public static void requireTrue(
            boolean condition,
            String messageKey,
            Object[] args,
            MessageSource messageSource) {
        if (!condition) {
            throw new ValidationException(createLocalizedMessage(messageKey, args, messageSource));
        }
    }

    /**
     * Validates that a condition is false
     */
    public static void requireFalse(
            boolean condition,
            String messageKey,
            Object[] args,
            MessageSource messageSource) {
        if (condition) {
            throw new ValidationException(createLocalizedMessage(messageKey, args, messageSource));
        }
    }
}

