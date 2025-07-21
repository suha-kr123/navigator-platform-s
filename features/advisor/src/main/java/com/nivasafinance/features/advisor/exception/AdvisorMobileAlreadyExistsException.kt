package com.nivasafinance.features.advisor.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when a advisor is not found by ID or other criteria.
 */
class AdvisorMobileAlreadyExistsException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.advisor.mobile.already.exists"
    }

    /**
     * Constructor for mobile number.
     * This is a convenience constructor for the common case of a ADVISOR not found by mobile number.
     */
    constructor(
        mobileNumber: String,
        messageSource: MessageSource
    ) : this(messageSource.getMessage(KEY, arrayOf(mobileNumber), locale))
}
