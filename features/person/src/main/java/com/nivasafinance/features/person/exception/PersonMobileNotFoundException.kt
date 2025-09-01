package com.nivasafinance.features.person.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when a person with the specified mobile number is not found.
 */
class PersonMobileNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.mobile.not.found"
    }

    /**
     * Constructor with message source for internationalization.
     */
    constructor(mobileNo: String, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(mobileNo), locale)
    )
}
