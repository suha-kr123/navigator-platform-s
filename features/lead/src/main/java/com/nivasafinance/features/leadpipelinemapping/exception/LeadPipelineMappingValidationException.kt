package com.nivasafinance.features.leadpipelinemapping.exception

import exception.ExceptionUtils
import exception.ValidationException
import org.springframework.context.MessageSource
import java.util.*

open class LeadPipelineMappingValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class LeadPipelineMappingLeadIdValidationException(
    leadId: UUID?,
    messageSource: MessageSource
) : LeadPipelineMappingValidationException(
    "error.lead.pipeline.mapping.lead.id.invalid",
    arrayOf(leadId?.toString() ?: "null"),
    messageSource
)

class LeadPipelineMappingPipelineKeyValidationException(
    pipelineKey: String?,
    messageSource: MessageSource
) : LeadPipelineMappingValidationException(
    "error.lead.pipeline.mapping.pipeline.key.invalid",
    arrayOf(pipelineKey ?: "null"),
    messageSource
)

class LeadPipelineMappingCurrentStageValidationException(
    currentStage: String?,
    messageSource: MessageSource
) : LeadPipelineMappingValidationException(
    "error.lead.pipeline.mapping.current.stage.invalid",
    arrayOf(currentStage ?: "null"),
    messageSource
)

class LeadPipelineMappingCannotUpdateCompletedException(
    mappingId: UUID,
    messageSource: MessageSource
) : LeadPipelineMappingValidationException(
    "error.lead.pipeline.mapping.cannot.update.completed",
    arrayOf(mappingId.toString()),
    messageSource
)

class LeadPipelineMappingCannotDeleteCompletedException(
    mappingId: UUID,
    messageSource: MessageSource
) : LeadPipelineMappingValidationException(
    "error.lead.pipeline.mapping.cannot.delete.completed",
    arrayOf(mappingId.toString()),
    messageSource
)
