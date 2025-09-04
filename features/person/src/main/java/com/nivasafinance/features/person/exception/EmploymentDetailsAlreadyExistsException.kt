package com.nivasafinance.features.person.exception

import exception.ConflictException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class EmploymentDetailsAlreadyExistsException(message: String) : ConflictException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.employment.details.already.exists"
    }

    constructor(personId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(personId.toString()), locale)
    )
}
