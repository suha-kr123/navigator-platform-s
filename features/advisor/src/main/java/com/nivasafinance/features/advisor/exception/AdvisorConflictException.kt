package com.nivasafinance.features.advisor.exception

import exception.ConflictException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class AdvisorConflictException(message: String) : ConflictException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val MOBILE_ALREADY_EXISTS_KEY = "error.advisor.mobile.already.exists"
        const val ADVISOR_CODE_ALREADY_EXISTS_KEY = "error.advisor.code.already.exists"
        const val PERSON_ALREADY_EXISTS_KEY = "error.advisor.person.already.exists"
    }

    constructor(messageKey: String, args: Array<Any>, messageSource: MessageSource) : this(
        messageSource.getMessage(messageKey, args, locale)
    )

    constructor(messageKey: String, messageSource: MessageSource) : this(
        messageSource.getMessage(messageKey, null, locale)
    )

    constructor(personId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(PERSON_ALREADY_EXISTS_KEY, arrayOf(personId), locale)
    )
}
