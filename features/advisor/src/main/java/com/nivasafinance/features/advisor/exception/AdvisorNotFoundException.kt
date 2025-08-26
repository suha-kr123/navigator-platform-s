package com.nivasafinance.features.advisor.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class AdvisorNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val ADVISOR_ID_NOT_FOUND_KEY = "error.advisor.id.not.found"
    }

    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(ADVISOR_ID_NOT_FOUND_KEY, arrayOf(id), locale)
    )
}
