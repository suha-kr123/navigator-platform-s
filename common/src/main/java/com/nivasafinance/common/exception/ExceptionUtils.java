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

    private static final String ROLLBACK_PHRASE = "rollback";
    private static final String ROLLBACK_ONLY_PHRASE = "rollback-only";

    /**
     * Returns a user-facing message from the exception chain, skipping generic
     * transaction/rollback messages so that validation or business errors (e.g.
     * "Only Active Lead can be put on dropoff") are shown instead of
     * "Transaction silently rolled back because it has been marked as rollback-only".
     */
    public static String getRootCauseMessage(Throwable t) {
        if (t == null) {
            return "";
        }
        // Prefer the first message in the chain that looks like a real error, not a transaction wrapper.
        Throwable current = t;
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null && !msg.isBlank() && !isGenericTransactionMessage(msg)) {
                return msg;
            }
            current = current.getCause();
        }
        // Fallback: root cause message or class name
        Throwable root = t;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        return (msg != null && !msg.isBlank()) ? msg : root.getClass().getSimpleName();
    }

    private static boolean isGenericTransactionMessage(String message) {
        if (message == null) {
            return true;
        }
        String lower = message.toLowerCase();
        return lower.contains(ROLLBACK_PHRASE) || lower.contains(ROLLBACK_ONLY_PHRASE);
    }
}

