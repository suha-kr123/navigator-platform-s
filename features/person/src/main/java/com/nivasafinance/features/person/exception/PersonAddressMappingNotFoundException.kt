package com.nivasafinance.features.person.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when a person address mapping is not found.
 */
class PersonAddressMappingNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.address.mapping.not.found"
    }

    /**
     * Constructor with person ID and address ID.
     * This is a convenience constructor for the common case of address mapping not found.
     */
    constructor(personId: UUID, addressId: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(personId.toString(), addressId.toString()), locale)
    )
}
