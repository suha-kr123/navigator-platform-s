package com.nivasafinance.features.master.codemaster.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
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
