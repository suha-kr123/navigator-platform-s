package event

import java.math.BigDecimal
import java.util.UUID

// Request Events
data class GetLeadRequest(
    val requestId: String,
    val leadId: UUID,
    val correlationId: String
)

data class ValidateLeadRequest(
    val requestId: String,
    val leadId: UUID,
    val correlationId: String
)

// Response Events
data class GetLeadResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val leadInfo: LeadInfo? = null,
    val error: String? = null
)

data class ValidateLeadResponse(
    val requestId: String,
    val correlationId: String,
    val isValid: Boolean,
    val error: String? = null
)

data class LeadInfo(
    val id: UUID,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val status: String?,
    val stage: String?
)
