package com.nivasafinance.features.master.pincode.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

/**
 * Exception thrown when a pincode is not found.
 */
class PincodeNotFoundException(message: String) : ResourceNotFoundException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val PINCODE_NOT_FOUND_KEY = "error.pincode.not.found"
    }

    /**
     * Constructor with a pincode.
     * This is a convenience constructor for the common case of a pincode not found.
     */
    constructor(
        pincode: String,
        messageSource: MessageSource
    ) : this(messageSource.getMessage(PINCODE_NOT_FOUND_KEY, arrayOf(pincode), locale))
}
