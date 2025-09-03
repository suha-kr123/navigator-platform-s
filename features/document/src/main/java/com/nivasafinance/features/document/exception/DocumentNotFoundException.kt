package com.nivasafinance.features.document.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.Locale
import java.util.UUID

class DocumentNotFoundException(
    documentId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    messageSource.getMessage("error.document.not.found", arrayOf(documentId), Locale.getDefault())
)
