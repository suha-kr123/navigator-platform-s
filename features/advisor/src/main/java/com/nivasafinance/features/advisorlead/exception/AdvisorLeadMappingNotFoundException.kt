package com.nivasafinance.features.advisorlead.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

class AdvisorLeadMappingNotFoundException(
    mappingId: UUID,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(
        "error.advisor.lead.mapping.not.found",
        arrayOf(mappingId.toString()),
        messageSource
    )
)
