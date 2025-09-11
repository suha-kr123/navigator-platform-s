package event

import java.math.BigDecimal
import java.util.UUID

// Request Events
data class CreatePaymentRequest(
    val requestId: String,
    val advisorId: UUID,
    val leadId: UUID,
    val amount: BigDecimal,
    val paymentType: String,
    val description: String?,
    val correlationId: String
)

data class GetPaymentRequest(
    val requestId: String,
    val paymentId: UUID,
    val correlationId: String
)

data class UpdatePaymentRequest(
    val requestId: String,
    val paymentId: UUID,
    val amount: BigDecimal?,
    val paymentType: String?,
    val description: String?,
    val correlationId: String
)

data class DeletePaymentRequest(
    val requestId: String,
    val paymentId: UUID,
    val correlationId: String
)

// Response Events
data class CreatePaymentResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val paymentInfo: PaymentInfo? = null,
    val error: String? = null
)

data class GetPaymentResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val paymentInfo: PaymentInfo? = null,
    val error: String? = null
)

data class UpdatePaymentResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val paymentInfo: PaymentInfo? = null,
    val error: String? = null
)

data class DeletePaymentResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val error: String? = null
)

data class PaymentInfo(
    val id: UUID,
    val advisorId: UUID,
    val leadId: UUID,
    val amount: BigDecimal,
    val paymentType: String,
    val status: String,
    val description: String?
)
