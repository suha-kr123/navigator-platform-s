package com.nivasafinance.features.advisorleadmapping.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class AdvisorLeadMappingNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.advisor.lead.mapping.not.found"
    }

    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(id.toString()), locale)
    )
}
