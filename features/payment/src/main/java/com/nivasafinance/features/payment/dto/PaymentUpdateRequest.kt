package com.nivasafinance.features.payment.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentUpdateRequest(
    val paymentStatus: String? = null,
    val amountPaid: BigDecimal? = null,
    val paidAt: LocalDateTime? = null,
    val paymentMethod: String? = null,
    val transactionId: String? = null,
    val remarks: String? = null,
    val extData: Map<String, Any>? = null
)
