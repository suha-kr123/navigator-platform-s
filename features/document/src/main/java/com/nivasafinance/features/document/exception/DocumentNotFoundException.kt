package com.nivasafinance.features.document.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class DocumentNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val DOCUMENT_NOT_FOUND_KEY = "error.document.not.found"
    }

    constructor(documentId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(DOCUMENT_NOT_FOUND_KEY, arrayOf(documentId), locale)
    )
}
