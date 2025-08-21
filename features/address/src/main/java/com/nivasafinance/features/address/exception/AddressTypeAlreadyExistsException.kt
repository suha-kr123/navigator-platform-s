package com.nivasafinance.features.address.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when a person already has an address of a specific type.
 */
class AddressTypeAlreadyExistsException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val KEY = "error.person.address.type.already.exists"
    }

    /**
     * Constructor with person ID and address type.
     * This is a convenience constructor for the common case of address type already existing.
     */
    constructor(personId: UUID, addressType: String, messageSource: MessageSource) : this(
        messageSource.getMessage(KEY, arrayOf(personId.toString(), addressType), locale)
    )
}
