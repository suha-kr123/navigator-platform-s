package com.nivasafinance.features.income.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when income details are not found by employment ID.
 */
class IncomeDetailsNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.income.details.not.found"
    }

    /**
     * Constructor with employment ID and message source for internationalization.
     */
    constructor(employmentId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(employmentId), locale)
    )
}
