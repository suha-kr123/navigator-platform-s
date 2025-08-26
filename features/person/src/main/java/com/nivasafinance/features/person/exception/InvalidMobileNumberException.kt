package com.nivasafinance.features.person.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when a mobile number format is invalid.
 */
class InvalidMobileNumberException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.invalid.mobile.number"
    }

    /**
     * Constructor with message source for internationalization.
     */
    constructor(messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, emptyArray(), locale)
    )
}
