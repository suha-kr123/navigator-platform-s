package com.nivasafinance.features.master.codemaster.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource

class CodeMasterNotFoundException(
    codeName: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.codemaster.code.name.not.found",
        arrayOf(codeName),
        messageSource
    )
)
