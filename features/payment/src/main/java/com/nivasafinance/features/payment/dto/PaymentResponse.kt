package com.nivasafinance.features.payment.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentResponse(
    val id: UUID,
    val paymentStatus: String,
    val amountPaid: BigDecimal?,
    val paidAt: LocalDateTime?,
    val paymentMethod: String?,
    val transactionId: String?,
    val remarks: String?,
    val extData: Map<String, Any>?
)
