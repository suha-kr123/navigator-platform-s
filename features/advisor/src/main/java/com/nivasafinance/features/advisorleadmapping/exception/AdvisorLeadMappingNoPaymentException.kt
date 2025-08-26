package com.nivasafinance.features.advisorleadmapping.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class AdvisorLeadMappingNoPaymentException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.advisor.lead.mapping.no.payment"
    }

    constructor(advisorId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(advisorId.toString()), locale)
    )
}
