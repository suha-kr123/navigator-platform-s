package com.nivasafinance.features.lead.lead.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when a lead is not found by ID or other criteria.
 */
class LeadNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val LEAD_ID_NOT_FOUND_KEY = "error.lead.id.not.found"
    }

    /**
     * Constructor with a lead ID.
     * This is a convenience constructor for the common case of a lead not found by ID.
     */
    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(LEAD_ID_NOT_FOUND_KEY, arrayOf(id), locale)
    )
}
