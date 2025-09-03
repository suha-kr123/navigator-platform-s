package com.nivasafinance.features.document.exception

import exception.ValidationException
import org.springframework.context.MessageSource
import java.util.Locale

class DocumentValidationException(
    message: String,
    messageSource: MessageSource
) : ValidationException(
    messageSource.getMessage("error.document.validation", arrayOf(message), Locale.getDefault())
)
