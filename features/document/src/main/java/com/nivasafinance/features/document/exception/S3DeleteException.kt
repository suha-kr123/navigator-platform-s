package com.nivasafinance.features.document.exception

import com.nivasafinance.common.exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class S3DeleteException(
    message: String
) : BadRequestException(message) {

    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val S3_DELETE_FAILED_KEY = "error.document.s3.delete.failed"
    }

    constructor(messageSource: MessageSource) : this(
        messageSource.getMessage(S3_DELETE_FAILED_KEY, arrayOf(""), locale)
    )

    constructor(error: String, messageSource: MessageSource) : this(
        messageSource.getMessage(S3_DELETE_FAILED_KEY, arrayOf(error), locale)
    )
}
