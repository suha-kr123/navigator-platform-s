package com.nivasafinance.features.document.exception

import exception.ConflictException
import org.springframework.context.MessageSource
import java.util.Locale

class DuplicateStorageKeyException(
    storageKey: String,
    messageSource: MessageSource
) : ConflictException(
    messageSource.getMessage("error.document.duplicate.storage.key", arrayOf(storageKey), Locale.getDefault())
)
