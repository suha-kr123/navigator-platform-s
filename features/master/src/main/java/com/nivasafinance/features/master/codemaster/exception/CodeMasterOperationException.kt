package com.nivasafinance.features.master.codemaster.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class CodeMasterOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
