package com.nivasafinance.features.address.exception

import exception.ValidationException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when an invalid address type is provided.
 */
class InvalidAddressTypeException(message: String) : ValidationException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.address.type.invalid"
        const val MANDATORY_KEY = "error.person.address.type.mandatory"

        /**
         * Creates an exception for invalid address type with valid types.
         */
        fun withValidTypes(
            invalidAddressType: String,
            validTypes: String,
            messageSource: MessageSource
        ): InvalidAddressTypeException {
            return InvalidAddressTypeException(
                messageSource.getMessage(KEY, arrayOf(invalidAddressType, validTypes), locale)
            )
        }

        /**
         * Creates an exception for mandatory field validation.
         */
        fun mandatory(messageSource: MessageSource): InvalidAddressTypeException {
            return InvalidAddressTypeException(
                messageSource.getMessage(MANDATORY_KEY, null, locale)
            )
        }

        /**
         * Creates an exception for enum usage without MessageSource.
         */
        fun forEnum(invalidType: String): InvalidAddressTypeException {
            val validTypes = com.nivasafinance.features.address.enum.AddressType.values()
                .joinToString(", ")
            return InvalidAddressTypeException(
                "Invalid address type: $invalidType. Valid types are: $validTypes"
            )
        }
    }
}
