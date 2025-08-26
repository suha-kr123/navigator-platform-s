package com.nivasafinance.features.person.exception

import com.nivasafinance.features.person.enum.IdentifierType
import exception.ConflictException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

class DuplicateIdentifierTypeException(message: String) : ConflictException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.identifier.duplicate"
    }

    constructor(personId: UUID, identifierType: IdentifierType, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(personId.toString(), identifierType.name), locale)
    )
}
