package com.nivasafinance.features.address.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when a person does not have an address of a specific type.
 */
class AddressTypeNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.address.type.not.found"
    }

    /**
     * Constructor with person ID and address type.
     * This is a convenience constructor for the common case of address type not found.
     */
    constructor(personId: UUID, addressType: String, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(personId.toString(), addressType), locale)
    )
}
