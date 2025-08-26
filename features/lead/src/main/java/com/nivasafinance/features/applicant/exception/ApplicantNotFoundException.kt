package com.nivasafinance.features.applicant.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class ApplicantNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val APPLICANT_ID_NOT_FOUND_KEY = "error.applicant.id.not.found"
    }

    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(APPLICANT_ID_NOT_FOUND_KEY, arrayOf(id), locale)
    )
}
