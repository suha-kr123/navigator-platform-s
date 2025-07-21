package com.nivasafinance.features.advisor.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when a advisor is not found by ID or other criteria.
 */
class AdvisorMobileNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val ADVISOR_MOBILE_NOT_FOUND_KEY = "error.advisor.mobile.not.found"
    }

    /**
     * Constructor for mobile number.
     * This is a convenience constructor for the common case of a ADVISOR not found by mobile number.
     */
    constructor(
        mobileNumber: String,
        messageSource: MessageSource
    ) : this(messageSource.getMessage(ADVISOR_MOBILE_NOT_FOUND_KEY, arrayOf(mobileNumber), locale))
}
