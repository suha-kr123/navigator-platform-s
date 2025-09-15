package com.nivasafinance.features.leadpipelinemapping.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class LeadPipelineMappingOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
