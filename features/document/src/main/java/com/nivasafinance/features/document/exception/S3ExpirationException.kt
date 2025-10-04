package com.nivasafinance.features.document.exception

import com.nivasafinance.common.exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class S3ExpirationException(
    message: String
) : BadRequestException(message) {

    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val S3_EXPIRATION_INVALID_KEY = "error.document.s3.expiration.invalid"
    }

    constructor(messageSource: MessageSource) : this(
        messageSource.getMessage(S3_EXPIRATION_INVALID_KEY, arrayOf(""), locale)
    )
}
