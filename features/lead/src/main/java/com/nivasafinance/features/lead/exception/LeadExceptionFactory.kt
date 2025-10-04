package com.nivasafinance.features.lead.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.UUID

object LeadExceptionFactory {

    fun notFound(leadId: UUID, messageSource: MessageSource): LeadNotFoundException {
        return LeadNotFoundException(leadId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): LeadOperationException {
        return LeadOperationException("error.lead.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): LeadOperationException {
        return LeadOperationException("error.lead.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): LeadOperationException {
        return LeadOperationException("error.lead.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): LeadOperationException {
        return LeadOperationException("error.lead.operation.retrieve", messageSource)
    }

    fun alreadyExists(leadId: UUID, messageSource: MessageSource): LeadAlreadyExistsException {
        return LeadAlreadyExistsException(leadId, messageSource)
    }

    fun amountInvalid(amount: BigDecimal?, messageSource: MessageSource): LeadAmountValidationException {
        return LeadAmountValidationException(amount, messageSource)
    }

    fun purposeInvalid(purpose: String?, messageSource: MessageSource): LeadPurposeValidationException {
        return LeadPurposeValidationException(purpose, messageSource)
    }

    fun productCodeInvalid(productCode: String?, messageSource: MessageSource): LeadProductCodeValidationException {
        return LeadProductCodeValidationException(productCode, messageSource)
    }

    fun statusInvalid(status: String?, messageSource: MessageSource): LeadStatusValidationException {
        return LeadStatusValidationException(status, messageSource)
    }

    fun cannotUpdateCompleted(leadId: UUID, messageSource: MessageSource): LeadCannotUpdateCompletedException {
        return LeadCannotUpdateCompletedException(leadId, messageSource)
    }

    fun cannotDeleteCompleted(leadId: UUID, messageSource: MessageSource): LeadCannotDeleteCompletedException {
        return LeadCannotDeleteCompletedException(leadId, messageSource)
    }

    fun invalidStatusTransition(currentStatus: String, newStatus: String, messageSource: MessageSource): LeadInvalidStatusTransitionException {
        return LeadInvalidStatusTransitionException(currentStatus, newStatus, messageSource)
    }

    fun validateLeadForCreation(
        requestedAmountRange: Map<String, BigDecimal>?,
        purpose: String?,
        productCode: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(requestedAmountRange, "requestedAmountRange", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(purpose, "purpose", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(productCode, "productCode", "error.invalid", messageSource)

        validateAmountRange(requestedAmountRange, messageSource)
        validatePurpose(purpose, messageSource)
        validateProductCode(productCode, messageSource)
    }

    fun validateLeadForUpdate(
        leadId: UUID?,
        requestedAmountRange: Map<String, BigDecimal>?,
        purpose: String?,
        productCode: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(leadId, "id", "error.invalid", messageSource)
        if (requestedAmountRange != null) {
            validateAmountRange(requestedAmountRange, messageSource)
        }
        if (purpose != null) {
            validatePurpose(purpose, messageSource)
        }
        if (productCode != null) {
            validateProductCode(productCode, messageSource)
        }
    }

    private fun validateAmountRange(amountRange: Map<String, BigDecimal>?, messageSource: MessageSource) {
        if (amountRange == null || amountRange.isEmpty()) {
            throw amountInvalid(null, messageSource)
        }
        
        val minAmount = amountRange["min"]
        val maxAmount = amountRange["max"]
        
        if (minAmount == null || minAmount <= BigDecimal.ZERO) {
            throw amountInvalid(minAmount, messageSource)
        }
        
        if (maxAmount == null || maxAmount <= BigDecimal.ZERO) {
            throw amountInvalid(maxAmount, messageSource)
        }
        
        if (minAmount > maxAmount) {
            throw amountInvalid(minAmount, messageSource)
        }
    }

    private fun validateAmount(amount: BigDecimal?, messageSource: MessageSource) {
        if (amount == null || amount <= BigDecimal.ZERO) {
            throw amountInvalid(amount, messageSource)
        }
    }

    private fun validatePurpose(purpose: String?, messageSource: MessageSource) {
        if (purpose.isNullOrBlank() || purpose.length > 40) {
            throw purposeInvalid(purpose, messageSource)
        }
    }

    private fun validateProductCode(productCode: String?, messageSource: MessageSource) {
        if (productCode.isNullOrBlank()) {
            throw productCodeInvalid(productCode, messageSource)
        }
    }

    fun validatePersonForCreation(
        mobileNumbers: List<com.nivasafinance.features.person.entity.MobileNumberDetails>?,
        messageSource: MessageSource
    ) {
        mobileNumbers?.let { numbers ->
            val hasPrimaryPhone = numbers.any { it.isPrimary == true }
            ExceptionUtils.requireTrue(
                hasPrimaryPhone,
                "error.person.primary.phone.required",
                null,
                messageSource
            )
        }
    }

    fun validatePhoneNumberUniqueness(
        phoneNumber: String?,
        existingPhones: MutableSet<String>,
        messageSource: MessageSource
    ) {
        val phone = phoneNumber ?: throw PersonValidationException("error.person.phone.number.required", arrayOf("phoneNumber"), messageSource)
        if (!existingPhones.add(phone)) {
            throw PersonValidationException("error.person.primary.phone.duplicate", arrayOf(phone), messageSource)
        }
    }

    fun personNotFound(personId: UUID, messageSource: MessageSource): PersonNotFoundException {
        return PersonNotFoundException(personId, messageSource)
    }

    fun taskNotBelongsToLead(taskId: UUID, leadId: UUID, messageSource: MessageSource): TaskNotBelongsToLeadException {
        return TaskNotBelongsToLeadException(taskId, leadId, messageSource)
    }

    fun notesNotBelongsToTask(notesId: UUID, taskId: UUID?, leadId: UUID, messageSource: MessageSource): NotesNotBelongsToTaskException {
        return NotesNotBelongsToTaskException(notesId, taskId, leadId, messageSource)
    }

    fun tasksNotBelongToLead(invalidTaskIds: List<UUID>, leadId: UUID, messageSource: MessageSource): TasksNotBelongToLeadException {
        return TasksNotBelongToLeadException(invalidTaskIds, leadId, messageSource)
    }

    fun taskRetrievalFailed(messageSource: MessageSource): LeadTaskOperationException {
        return LeadTaskOperationException("error.lead.task.retrieval.failed", null, messageSource)
    }

    fun taskRetrievalFailedForLead(leadId: UUID, messageSource: MessageSource): LeadTaskOperationException {
        return LeadTaskOperationException("error.lead.task.retrieval.failed.for.lead", arrayOf(leadId.toString()), messageSource)
    }

    fun taskCreationFailedForLead(leadId: UUID, messageSource: MessageSource): LeadTaskOperationException {
        return LeadTaskOperationException("error.lead.task.creation.failed.for.lead", arrayOf(leadId.toString()), messageSource)
    }

    fun taskUpdateFailed(taskId: UUID, leadId: UUID, messageSource: MessageSource): LeadTaskOperationException {
        return LeadTaskOperationException("error.lead.task.update.failed", arrayOf(taskId.toString(), leadId.toString()), messageSource)
    }

    fun addressNotFoundForLead(leadId: UUID, messageSource: MessageSource): LeadValidationException {
        return LeadValidationException("error.lead.address.not.found.for.lead", arrayOf(leadId.toString()), messageSource)
    }

    fun taskDeletionFailed(taskId: UUID, leadId: UUID, messageSource: MessageSource): LeadTaskOperationException {
        return LeadTaskOperationException("error.lead.task.deletion.failed", arrayOf(taskId.toString(), leadId.toString()), messageSource)
    }

    fun taskRetrievalFailedForTask(taskId: UUID, leadId: UUID, messageSource: MessageSource): LeadTaskOperationException {
        return LeadTaskOperationException("error.lead.task.retrieval.failed.for.task", arrayOf(taskId.toString(), leadId.toString()), messageSource)
    }

    fun identifierRetrievalFailed(identifierId: UUID, leadId: UUID, messageSource: MessageSource): LeadIdentifierOperationException {
        return LeadIdentifierOperationException("error.lead.identifier.retrieval.failed", arrayOf(identifierId.toString(), leadId.toString()), messageSource)
    }

    fun identifierCreationFailed(leadId: UUID, messageSource: MessageSource): LeadIdentifierOperationException {
        return LeadIdentifierOperationException("error.lead.identifier.creation.failed", arrayOf(leadId.toString()), messageSource)
    }

    fun identifierUpdateFailed(identifierId: UUID, leadId: UUID, messageSource: MessageSource): LeadIdentifierOperationException {
        return LeadIdentifierOperationException("error.lead.identifier.update.failed", arrayOf(identifierId.toString(), leadId.toString()), messageSource)
    }
}
