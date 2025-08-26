package com.nivasafinance.features.advisor.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class AdvisorMobileNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val ADVISOR_MOBILE_NOT_FOUND_KEY = "error.advisor.mobile.not.found"
    }

    constructor(
        mobileNumber: String,
        messageSource: MessageSource
    ) : this(messageSource.getMessage(ADVISOR_MOBILE_NOT_FOUND_KEY, arrayOf(mobileNumber), locale))
}
