package com.nivasafinance.features.advisorleadmapping.client

import java.math.BigDecimal

data class PaymentUpdateRequest(
    val amount: BigDecimal?,
    val paymentType: String?,
    val description: String?
)
