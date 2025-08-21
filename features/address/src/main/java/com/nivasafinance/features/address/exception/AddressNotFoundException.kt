package com.nivasafinance.features.address.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale
import java.util.UUID

/**
 * Exception thrown when an address is not found by ID or other criteria.
 */
class AddressNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val ADDRESS_ID_NOT_FOUND_KEY = "error.address.id.not.found"
    }

    /**
     * Constructor with an address ID.
     * This is a convenience constructor for the common case of an address not found by ID.
     */
    @Suppress("ArrayPrimitive")
    constructor(id: UUID, messageSource: MessageSource) : this(
        messageSource.getMessage(ADDRESS_ID_NOT_FOUND_KEY, arrayOf(id), locale)
    )
}
