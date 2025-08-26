package com.nivasafinance.features.person.exception

import exception.ConflictException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when attempting to create a person with a primary mobile number that already exists in the database.
 */
class PrimaryMobileNumberAlreadyExistsException(message: String) : ConflictException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.primary.mobile.already.exists"
    }

    /**
     * Constructor with message source for internationalization.
     */
    constructor(messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, emptyArray(), locale)
    )
}
