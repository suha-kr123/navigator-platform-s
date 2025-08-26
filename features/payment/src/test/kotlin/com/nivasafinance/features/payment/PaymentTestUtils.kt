package com.nivasafinance.features.payment

import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentData
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.entity.Payment
import com.nivasafinance.features.payment.enum.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val FIXED_TEST_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

object PaymentTestUtils {

    fun createTestPayment(
        id: UUID = UUID.randomUUID(),
        paymentStatus: PaymentStatus = PaymentStatus.PENDING_PAYMENT,
        amountPaid: BigDecimal? = BigDecimal("1000.00"),
        paidAt: LocalDateTime? = FIXED_TEST_TIME,
        paymentMethod: String? = "CREDIT_CARD",
        transactionId: String? = "TXN123456",
        remarks: String? = "Test payment",
        extData: Map<String, Any>? = mapOf("key" to "value")
    ): Payment {
        return Payment(
            id = id,
            paymentStatus = paymentStatus,
            amountPaid = amountPaid,
            paidAt = paidAt,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            remarks = remarks,
            extData = extData
        )
    }

    fun createTestPaymentData(
        id: UUID = UUID.randomUUID(),
        paymentStatus: PaymentStatus = PaymentStatus.PENDING_PAYMENT,
        amountPaid: BigDecimal? = BigDecimal("1000.00"),
        paidAt: LocalDateTime? = FIXED_TEST_TIME,
        paymentMethod: String? = "CREDIT_CARD",
        transactionId: String? = "TXN123456",
        remarks: String? = "Test payment",
        extData: Map<String, Any>? = mapOf("key" to "value")
    ): PaymentData {
        return PaymentData(
            id = id,
            paymentStatus = paymentStatus,
            amountPaid = amountPaid,
            paidAt = paidAt,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            remarks = remarks,
            extData = extData
        )
    }

    fun createTestPaymentCreateRequest(
        paymentStatus: String? = "PENDING_PAYMENT",
        amountPaid: BigDecimal? = BigDecimal("1000.00"),
        paidAt: LocalDateTime? = FIXED_TEST_TIME,
        paymentMethod: String? = "CREDIT_CARD",
        transactionId: String? = "TXN123456",
        remarks: String? = "Test payment",
        extData: Map<String, Any>? = mapOf("key" to "value")
    ): PaymentCreateRequest {
        return PaymentCreateRequest(
            paymentStatus = paymentStatus,
            amountPaid = amountPaid,
            paidAt = paidAt,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            remarks = remarks,
            extData = extData
        )
    }

    fun createTestPaymentUpdateRequest(
        paymentStatus: String? = "PAID",
        amountPaid: BigDecimal? = BigDecimal("1000.00"),
        paidAt: LocalDateTime? = FIXED_TEST_TIME,
        paymentMethod: String? = "CREDIT_CARD",
        transactionId: String? = "TXN123456",
        remarks: String? = "Updated payment",
        extData: Map<String, Any>? = mapOf("key" to "updated_value")
    ): PaymentUpdateRequest {
        return PaymentUpdateRequest(
            paymentStatus = paymentStatus,
            amountPaid = amountPaid,
            paidAt = paidAt,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            remarks = remarks,
            extData = extData
        )
    }

    fun createTestPaymentResponse(
        id: UUID = UUID.randomUUID(),
        paymentStatus: String = "PENDING_PAYMENT",
        amountPaid: BigDecimal? = BigDecimal("1000.00"),
        paidAt: LocalDateTime? = FIXED_TEST_TIME,
        paymentMethod: String? = "CREDIT_CARD",
        transactionId: String? = "TXN123456",
        remarks: String? = "Test payment",
        extData: Map<String, Any>? = mapOf("key" to "value")
    ): PaymentResponse {
        return PaymentResponse(
            id = id,
            paymentStatus = paymentStatus,
            amountPaid = amountPaid,
            paidAt = paidAt,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            remarks = remarks,
            extData = extData
        )
    }
}
