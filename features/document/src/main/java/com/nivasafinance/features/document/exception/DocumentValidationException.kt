package com.nivasafinance.features.document.exception

import exception.ValidationException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class DocumentValidationException(message: String) : ValidationException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val DOCUMENT_VALIDATION_KEY = "error.document.validation"
    }

    constructor(message: String, messageSource: MessageSource) : this(
        messageSource.getMessage(DOCUMENT_VALIDATION_KEY, arrayOf(message), locale)
    )
}
