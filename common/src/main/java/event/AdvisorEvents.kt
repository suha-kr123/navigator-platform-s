package event

import java.util.UUID

// Request Events
data class GetAdvisorRequest(
    val requestId: String,
    val advisorId: UUID,
    val correlationId: String
)

data class ValidateAdvisorRequest(
    val requestId: String,
    val advisorId: UUID,
    val correlationId: String
)

data class GetAdvisorByMobileRequest(
    val requestId: String,
    val mobileNumber: String,
    val correlationId: String
)

// Response Events
data class GetAdvisorResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val advisorInfo: AdvisorInfo? = null,
    val error: String? = null
)

data class ValidateAdvisorResponse(
    val requestId: String,
    val correlationId: String,
    val isValid: Boolean,
    val error: String? = null
)

data class GetAdvisorByMobileResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val advisorInfo: AdvisorInfo? = null,
    val error: String? = null
)

data class AdvisorInfo(
    val id: UUID,
    val advisorCode: String?,
    val status: String,
    val isEmployee: Boolean
)
