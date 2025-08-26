package com.nivasafinance.features.advisor.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class AdvisorMobileAlreadyExistsException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.advisor.mobile.already.exists"
    }

    constructor(
        mobileNumber: String,
        messageSource: MessageSource
    ) : this(messageSource.getMessage(KEY, arrayOf(mobileNumber), locale))
}
