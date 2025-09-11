package com.nivasafinance.features.advisorleadmapping.client

import java.math.BigDecimal
import java.util.UUID

data class PaymentResponse(
    val id: UUID,
    val advisorId: UUID,
    val leadId: UUID,
    val amount: BigDecimal,
    val paymentType: String,
    val status: String,
    val description: String?
)
