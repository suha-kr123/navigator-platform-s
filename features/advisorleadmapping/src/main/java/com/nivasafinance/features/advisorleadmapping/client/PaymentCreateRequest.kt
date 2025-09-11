package com.nivasafinance.features.advisorleadmapping.client

import java.math.BigDecimal
import java.util.UUID

data class PaymentCreateRequest(
    val advisorId: UUID,
    val leadId: UUID,
    val amount: BigDecimal,
    val paymentType: String,
    val description: String?
)
