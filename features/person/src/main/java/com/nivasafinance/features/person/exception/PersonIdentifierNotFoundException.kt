package com.nivasafinance.features.person.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class PersonIdentifierNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.identifier.not.found"
    }

    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(id.toString()), locale)
    )
}
