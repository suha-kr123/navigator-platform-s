package com.nivasafinance.features.person.exception

import com.nivasafinance.features.person.enum.IdentifierType
import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class InvalidIdentifierTypeException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.identifier.type.invalid"

        /**
         * Creates an exception for invalid identifier type with valid types.
         */
        fun withValidTypes(invalidType: String, messageSource: MessageSource): InvalidIdentifierTypeException {
            return InvalidIdentifierTypeException(
                messageSource.getMessage(KEY, arrayOf(invalidType, IdentifierType.values().joinToString(", ")), locale)
            )
        }

        /**
         * Creates an exception for enum usage without MessageSource.
         */
        fun forEnum(invalidType: String): InvalidIdentifierTypeException {
            return InvalidIdentifierTypeException(
                "Invalid identifier type: $invalidType. Valid types are: ${IdentifierType.values().joinToString(", ")}"
            )
        }
    }
}
