package com.nivasafinance.features.person.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class EmploymentDetailsNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale() ?: Locale.getDefault()
        const val KEY = "error.employment.details.not.found"
    }

    constructor(personId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(
            KEY,
            arrayOf(personId.toString()),
            locale
        )
    )
}
