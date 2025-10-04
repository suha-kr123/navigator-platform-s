package com.nivasafinance.features.document.exception

import com.nivasafinance.common.exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class S3UrlGenerationException(
    message: String
) : BadRequestException(message) {

    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val S3_URL_GENERATION_FAILED_KEY = "error.document.s3.url.generation.failed"
    }

    constructor(messageSource: MessageSource) : this(
        messageSource.getMessage(S3_URL_GENERATION_FAILED_KEY, arrayOf(""), locale)
    )

    constructor(error: String, messageSource: MessageSource) : this(
        messageSource.getMessage(S3_URL_GENERATION_FAILED_KEY, arrayOf(error), locale)
    )
}
