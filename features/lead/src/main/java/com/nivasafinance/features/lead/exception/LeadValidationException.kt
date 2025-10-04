package com.nivasafinance.features.lead.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ValidationException
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.UUID

open class LeadValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class LeadAmountValidationException(
    amount: BigDecimal?,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.amount.invalid",
    arrayOf(amount?.toString() ?: "null"),
    messageSource
)

class LeadPurposeValidationException(
    purpose: String?,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.purpose.invalid",
    arrayOf(purpose ?: "null"),
    messageSource
)

class LeadProductCodeValidationException(
    productCode: String?,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.product.code.invalid",
    arrayOf(productCode ?: "null"),
    messageSource
)

class LeadStatusValidationException(
    status: String?,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.status.invalid",
    arrayOf(status ?: "null"),
    messageSource
)

class LeadCannotUpdateCompletedException(
    leadId: UUID,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.cannot.update.completed",
    arrayOf(leadId.toString()),
    messageSource
)

class LeadCannotDeleteCompletedException(
    leadId: UUID,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.cannot.delete.completed",
    arrayOf(leadId.toString()),
    messageSource
)

class LeadInvalidStatusTransitionException(
    currentStatus: String,
    newStatus: String,
    messageSource: MessageSource
) : LeadValidationException(
    "error.lead.invalid.status.transition",
    arrayOf(currentStatus, newStatus),
    messageSource
)

class PersonNotFoundException(
    personId: UUID,
    messageSource: MessageSource
) : LeadValidationException(
    "error.person.not.found",
    arrayOf(personId.toString()),
    messageSource
)

class TaskNotBelongsToLeadException(
    taskId: UUID,
    leadId: UUID,
    messageSource: MessageSource
) : LeadValidationException(
    "error.task.not.belongs.to.lead",
    arrayOf(taskId.toString(), leadId.toString()),
    messageSource
)

class NotesNotBelongsToTaskException(
    notesId: UUID,
    taskId: UUID?,
    leadId: UUID,
    messageSource: MessageSource
) : LeadValidationException(
    "error.notes.not.belongs.to.task",
    arrayOf(notesId.toString(), taskId?.toString() ?: "null", leadId.toString()),
    messageSource
)

class PersonValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : LeadValidationException(messageKey, args, messageSource)

class TasksNotBelongToLeadException(
    invalidTaskIds: List<UUID>,
    leadId: UUID,
    messageSource: MessageSource
) : LeadValidationException(
    "error.tasks.not.belong.to.lead",
    arrayOf(invalidTaskIds.joinToString(", "), leadId.toString()),
    messageSource
)

class LeadTaskOperationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : LeadValidationException(messageKey, args, messageSource)

class LeadIdentifierOperationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : LeadValidationException(messageKey, args, messageSource)
