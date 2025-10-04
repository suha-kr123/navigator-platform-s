package com.nivasafinance.common.exception

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Utility class for common exception handling patterns
 */
object ExceptionUtils {

    private val locale: Locale = LocaleContextHolder.getLocale()

    /**
     * Creates a localized message using MessageSource
     */
    fun createLocalizedMessage(
        messageKey: String,
        args: Array<Any>? = null,
        messageSource: MessageSource
    ): String {
        return messageSource.getMessage(messageKey, args, locale)
    }

    /**
     * Validates that a required field is not null
     */
    fun requireNotNull(
        value: Any?,
        fieldName: String,
        messageKey: String,
        messageSource: MessageSource
    ): Nothing? {
        if (value == null) {
            throw ValidationException(createLocalizedMessage(messageKey, arrayOf(fieldName), messageSource))
        }
        return null
    }

    /**
     * Validates that a required field is not blank (for strings)
     */
    fun requireNotBlank(
        value: String?,
        fieldName: String,
        messageKey: String,
        messageSource: MessageSource
    ): Nothing? {
        if (value.isNullOrBlank()) {
            throw ValidationException(createLocalizedMessage(messageKey, arrayOf(fieldName), messageSource))
        }
        return null
    }

    /**
     * Validates that a condition is true
     */
    fun requireTrue(
        condition: Boolean,
        messageKey: String,
        args: Array<Any>? = null,
        messageSource: MessageSource
    ): Nothing? {
        if (!condition) {
            throw ValidationException(createLocalizedMessage(messageKey, args, messageSource))
        }
        return null
    }

    /**
     * Validates that a condition is false
     */
    fun requireFalse(
        condition: Boolean,
        messageKey: String,
        args: Array<Any>? = null,
        messageSource: MessageSource
    ): Nothing? {
        if (condition) {
            throw ValidationException(createLocalizedMessage(messageKey, args, messageSource))
        }
        return null
    }
}
