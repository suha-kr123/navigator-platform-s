package com.nivasafinance.features.document.exception

import exception.ValidationException
import org.springframework.context.MessageSource
import java.util.Locale

class InvalidFileTypeException(
    fileType: String,
    allowedTypes: String,
    messageSource: MessageSource
) : ValidationException(
    messageSource.getMessage("error.document.invalid.file.type", arrayOf(fileType, allowedTypes), Locale.getDefault())
)
