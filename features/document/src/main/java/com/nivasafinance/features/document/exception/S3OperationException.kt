package com.nivasafinance.features.document.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class S3OperationException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val S3_UPLOAD_FAILED_KEY = "error.document.s3.upload.failed"
        const val S3_DOWNLOAD_FAILED_KEY = "error.document.s3.download.failed"
        const val S3_DELETE_FAILED_KEY = "error.document.s3.delete.failed"
        const val S3_URL_GENERATION_FAILED_KEY = "error.document.s3.url.generation.failed"
        const val S3_EXPIRATION_INVALID_KEY = "error.document.s3.expiration.invalid"
    }

    constructor(operation: String, messageSource: MessageSource) : this(
        messageSource.getMessage(operation, arrayOf(""), locale)
    )

    constructor(operation: String, error: String, messageSource: MessageSource) : this(
        messageSource.getMessage(operation, arrayOf(error), locale)
    )
}
