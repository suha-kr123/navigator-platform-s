package com.nivasafinance.features.lead.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.*

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
        requestedAmount: BigDecimal?,
        purpose: String?,
        productCode: String?,
        status: String?,
        sourcingChannel: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(requestedAmount, "requestedAmount", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(purpose, "purpose", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(productCode, "productCode", "error.invalid", messageSource)

        validateAmount(requestedAmount, messageSource)
        validatePurpose(purpose, messageSource)
        validateProductCode(productCode, messageSource)
    }

    fun validateLeadForUpdate(
        leadId: UUID?,
        requestedAmount: BigDecimal?,
        purpose: String?,
        productCode: String?,
        status: String?,
        sourcingChannel: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(leadId, "id", "error.invalid", messageSource)
        validateLeadForCreation(requestedAmount, purpose, productCode, status, sourcingChannel, messageSource)
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
}
