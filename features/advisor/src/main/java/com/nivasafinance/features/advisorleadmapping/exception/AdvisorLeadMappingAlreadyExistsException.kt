package com.nivasafinance.features.advisorleadmapping.exception

import exception.ConflictException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class AdvisorLeadMappingAlreadyExistsException(message: String) : ConflictException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.advisor.lead.mapping.already.exists"
    }

    constructor(advisorId: UUID, leadId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(advisorId.toString(), leadId.toString()), locale)
    )
}
