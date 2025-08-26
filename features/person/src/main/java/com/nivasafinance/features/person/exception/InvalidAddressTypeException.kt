package com.nivasafinance.features.person.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when an invalid address type is provided.
 */
class InvalidAddressTypeException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.address.type.invalid"
        const val MANDATORY_KEY = "error.person.address.type.mandatory"

        /**
         * Creates an exception for invalid address type with valid types.
         */
        fun withValidTypes(
            addressType: String,
            validTypes: String,
            messageSource: MessageSource
        ): InvalidAddressTypeException {
            return InvalidAddressTypeException(
                messageSource.getMessage(KEY, arrayOf(addressType, validTypes), locale)
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
    }
}
