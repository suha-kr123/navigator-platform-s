package com.nivasafinance.features.applicant.exception

import exception.ConflictException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class PrimaryApplicantAlreadyExistsException(message: String) : ConflictException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val PRIMARY_APPLICANT_EXISTS_KEY = "error.applicant.primary.already.exists"
    }

    constructor(leadId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(PRIMARY_APPLICANT_EXISTS_KEY, arrayOf(leadId), locale)
    )
}
