package com.nivasafinance.features.leadpipelinemapping.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.*

object LeadPipelineMappingExceptionFactory {

    fun notFound(mappingId: UUID, messageSource: MessageSource): LeadPipelineMappingNotFoundException {
        return LeadPipelineMappingNotFoundException(mappingId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): LeadPipelineMappingOperationException {
        return LeadPipelineMappingOperationException("error.lead.pipeline.mapping.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): LeadPipelineMappingOperationException {
        return LeadPipelineMappingOperationException("error.lead.pipeline.mapping.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): LeadPipelineMappingOperationException {
        return LeadPipelineMappingOperationException("error.lead.pipeline.mapping.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): LeadPipelineMappingOperationException {
        return LeadPipelineMappingOperationException("error.lead.pipeline.mapping.operation.retrieve", messageSource)
    }

    fun alreadyExists(leadId: UUID, pipelineKey: String, messageSource: MessageSource): LeadPipelineMappingAlreadyExistsException {
        return LeadPipelineMappingAlreadyExistsException(leadId, pipelineKey, messageSource)
    }

    fun duplicateStage(leadId: UUID, stage: String, messageSource: MessageSource): LeadPipelineMappingDuplicateStageException {
        return LeadPipelineMappingDuplicateStageException(leadId, stage, messageSource)
    }

    fun leadIdInvalid(leadId: UUID?, messageSource: MessageSource): LeadPipelineMappingLeadIdValidationException {
        return LeadPipelineMappingLeadIdValidationException(leadId, messageSource)
    }

    fun pipelineKeyInvalid(pipelineKey: String?, messageSource: MessageSource): LeadPipelineMappingPipelineKeyValidationException {
        return LeadPipelineMappingPipelineKeyValidationException(pipelineKey, messageSource)
    }

    fun currentStageInvalid(currentStage: String?, messageSource: MessageSource): LeadPipelineMappingCurrentStageValidationException {
        return LeadPipelineMappingCurrentStageValidationException(currentStage, messageSource)
    }

    fun cannotUpdateCompleted(mappingId: UUID, messageSource: MessageSource): LeadPipelineMappingCannotUpdateCompletedException {
        return LeadPipelineMappingCannotUpdateCompletedException(mappingId, messageSource)
    }

    fun cannotDeleteCompleted(mappingId: UUID, messageSource: MessageSource): LeadPipelineMappingCannotDeleteCompletedException {
        return LeadPipelineMappingCannotDeleteCompletedException(mappingId, messageSource)
    }

    fun validateLeadPipelineMappingForCreation(
        leadId: UUID?,
        pipelineKey: String?,
        currentStage: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(leadId, "leadId", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(pipelineKey, "pipelineKey", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(currentStage, "currentStage", "error.invalid", messageSource)
        
        validateLeadId(leadId, messageSource)
        validatePipelineKey(pipelineKey, messageSource)
        validateCurrentStage(currentStage, messageSource)
    }

    fun validateLeadPipelineMappingForUpdate(
        mappingId: UUID?,
        leadId: UUID?,
        pipelineKey: String?,
        currentStage: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(mappingId, "id", "error.invalid", messageSource)
        validateLeadPipelineMappingForCreation(leadId, pipelineKey, currentStage, messageSource)
    }

    private fun validateLeadId(leadId: UUID?, messageSource: MessageSource) {
        if (leadId == null) {
            throw leadIdInvalid(leadId, messageSource)
        }
    }

    private fun validatePipelineKey(pipelineKey: String?, messageSource: MessageSource) {
        if (pipelineKey.isNullOrBlank()) {
            throw pipelineKeyInvalid(pipelineKey, messageSource)
        }
    }

    private fun validateCurrentStage(currentStage: String?, messageSource: MessageSource) {
        if (currentStage.isNullOrBlank()) {
            throw currentStageInvalid(currentStage, messageSource)
        }
    }
}
