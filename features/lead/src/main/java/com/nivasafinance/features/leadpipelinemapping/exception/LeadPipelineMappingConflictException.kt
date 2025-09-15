package com.nivasafinance.features.leadpipelinemapping.exception

import exception.ConflictException
import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.*

open class LeadPipelineMappingConflictException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ConflictException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class LeadPipelineMappingAlreadyExistsException(
    leadId: UUID,
    pipelineKey: String,
    messageSource: MessageSource
) : LeadPipelineMappingConflictException(
    "error.lead.pipeline.mapping.already.exists",
    arrayOf(leadId.toString(), pipelineKey),
    messageSource
)

class LeadPipelineMappingDuplicateStageException(
    leadId: UUID,
    stage: String,
    messageSource: MessageSource
) : LeadPipelineMappingConflictException(
    "error.lead.pipeline.mapping.duplicate.stage",
    arrayOf(leadId.toString(), stage),
    messageSource
)
