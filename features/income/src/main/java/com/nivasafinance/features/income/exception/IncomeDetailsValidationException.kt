package com.nivasafinance.features.income.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when income details validation fails.
 */
class IncomeDetailsValidationException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.income.details.validation"
    }

    /**
     * Constructor with message source for internationalization.
     */
    constructor(messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, emptyArray(), locale)
    )

    /**
     * Constructor with custom validation message.
     */
    constructor(validationMessage: String, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(validationMessage), locale)
    )
}
