package com.nivasafinance.features.payment.dto

import com.nivasafinance.features.payment.entity.Payment
import com.nivasafinance.features.payment.enum.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentData(
    val id: UUID?,
    val paymentStatus: PaymentStatus,
    val amountPaid: BigDecimal?,
    val paidAt: LocalDateTime?,
    val paymentMethod: String?,
    val transactionId: String?,
    val remarks: String?,
    val extData: Map<String, Any>?
) {
    companion object {
        fun fromEntity(payment: Payment): PaymentData {
            return PaymentData(
                id = payment.id,
                paymentStatus = payment.paymentStatus,
                amountPaid = payment.amountPaid,
                paidAt = payment.paidAt,
                paymentMethod = payment.paymentMethod,
                transactionId = payment.transactionId,
                remarks = payment.remarks,
                extData = payment.extData
            )
        }
    }
}
