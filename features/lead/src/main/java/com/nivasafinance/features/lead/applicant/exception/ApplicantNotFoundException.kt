package com.nivasafinance.features.lead.applicant.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when an applicant is not found by ID or other criteria.
 */
class ApplicantNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val APPLICANT_ID_NOT_FOUND_KEY = "error.applicant.id.not.found"
    }

    /**
     * Constructor with an applicant ID.
     * This is a convenience constructor for the common case of an applicant not found by ID.
     */
    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(APPLICANT_ID_NOT_FOUND_KEY, arrayOf(id), locale)
    )
}
