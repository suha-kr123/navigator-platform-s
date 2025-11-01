package com.nivasafinance.features.master.codemaster.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource

class CodeValueKeyNotFoundException(
    key: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.codevalue.key.not.found",
        arrayOf(key),
        messageSource
    )
)
