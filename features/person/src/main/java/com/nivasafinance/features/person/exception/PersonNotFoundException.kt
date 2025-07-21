package com.nivasafinance.features.person.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when a person is not found by ID or other criteria.
 */
class PersonNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.id.not.found"
    }

    /**
     * Constructor with a person ID.
     * This is a convenience constructor for the common case of a person not found by ID.
     */
    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(id), locale)
    )
}
