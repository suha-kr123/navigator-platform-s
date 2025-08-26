package com.nivasafinance.features.advisor.exception

import exception.ValidationException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class AdvisorValidationException(message: String) : ValidationException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val PERSON_ID_REQUIRED_KEY = "error.advisor.person.id.required"
        const val ADVISOR_CODE_REQUIRED_KEY = "error.advisor.code.required"
    }

    constructor(messageKey: String, args: Array<Any>, messageSource: MessageSource) : this(
        messageSource.getMessage(messageKey, args, locale)
    )

    constructor(messageKey: String, messageSource: MessageSource) : this(
        messageSource.getMessage(messageKey, null, locale)
    )
}
