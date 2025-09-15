package com.nivasafinance.features.leadpipelinemapping.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class LeadPipelineMappingNotFoundException(
    mappingId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    exception.ExceptionUtils.createLocalizedMessage(
        "error.lead.pipeline.mapping.not.found",
        arrayOf(mappingId.toString()),
        messageSource
    )
)
