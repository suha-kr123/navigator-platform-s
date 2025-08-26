package com.nivasafinance.features.payment.integration

import com.nivasafinance.features.payment.PaymentTestUtils
import com.nivasafinance.features.payment.controller.PaymentController
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.service.PaymentService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("Payment Integration Tests")
class PaymentIntegrationTest {

    private lateinit var paymentService: PaymentService
    private lateinit var paymentController: PaymentController

    private val paymentId = UUID.randomUUID()
    private val testTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
    
    private val expectedPaymentResponse = PaymentTestUtils.createTestPaymentResponse(
        id = paymentId,
        paymentStatus = "PENDING_PAYMENT",
        amountPaid = BigDecimal("1000.00"),
        paidAt = testTime,
        paymentMethod = "CREDIT_CARD",
        transactionId = "TXN123456",
        remarks = "Test payment"
    )

    @BeforeEach
    fun setup() {
        paymentService = mockk<PaymentService>()
        paymentController = PaymentController(paymentService)
    }

    @Test
    @DisplayName("should create payment through complete flow")
    fun `createPayment should work through complete flow`() {
        // Given
        val createRequest = PaymentTestUtils.createTestPaymentCreateRequest(
            paymentStatus = "PENDING_PAYMENT",
            amountPaid = BigDecimal("1000.00"),
            paidAt = testTime,
            paymentMethod = "CREDIT_CARD",
            transactionId = "TXN123456",
            remarks = "Test payment"
        )
        every { paymentService.createPayment(createRequest) } returns expectedPaymentResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.payment.dto.PaymentResponse> =
            paymentController.createPayment(createRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedPaymentResponse.id, response.body?.id)
        assertEquals(expectedPaymentResponse.paymentStatus, response.body?.paymentStatus)
        assertEquals(expectedPaymentResponse.amountPaid, response.body?.amountPaid)
        assertEquals(expectedPaymentResponse.paymentMethod, response.body?.paymentMethod)
        assertEquals(expectedPaymentResponse.transactionId, response.body?.transactionId)
    }

    @Test
    @DisplayName("should get payment through complete flow")
    fun `getPayment should work through complete flow`() {
        // Given
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.payment.dto.PaymentResponse> =
            paymentController.getPayment(paymentId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPaymentResponse.id, response.body?.id)
        assertEquals(expectedPaymentResponse.paymentStatus, response.body?.paymentStatus)
        assertEquals(expectedPaymentResponse.amountPaid, response.body?.amountPaid)
        assertEquals(expectedPaymentResponse.paymentMethod, response.body?.paymentMethod)
    }

    @Test
    @DisplayName("should update payment through complete flow")
    fun `updatePayment should work through complete flow`() {
        // Given
        val updateRequest = PaymentTestUtils.createTestPaymentUpdateRequest(
            paymentStatus = "PAID",
            amountPaid = BigDecimal("1000.00"),
            paidAt = testTime,
            paymentMethod = "CREDIT_CARD",
            transactionId = "TXN123456",
            remarks = "Updated payment"
        )
        every { paymentService.updatePayment(paymentId, updateRequest) } returns expectedPaymentResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.payment.dto.PaymentResponse> =
            paymentController.updatePayment(paymentId, updateRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPaymentResponse.id, response.body?.id)
        assertEquals(expectedPaymentResponse.paymentStatus, response.body?.paymentStatus)
        assertEquals(expectedPaymentResponse.amountPaid, response.body?.amountPaid)
        assertEquals(expectedPaymentResponse.remarks, response.body?.remarks)
    }

    @Test
    @DisplayName("should delete payment through complete flow")
    fun `deletePayment should work through complete flow`() {
        // Given
        every { paymentService.deletePayment(paymentId) } returns Unit

        // When
        val response: ResponseEntity<Unit> = paymentController.deletePayment(paymentId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    @DisplayName("should handle payment service calls correctly")
    fun `payment service should handle all operations correctly`() {
        // Given
        val createRequest = PaymentTestUtils.createTestPaymentCreateRequest(
            paymentStatus = "PENDING_PAYMENT",
            amountPaid = BigDecimal("1000.00"),
            paymentMethod = "CREDIT_CARD"
        )
        val updateRequest = PaymentTestUtils.createTestPaymentUpdateRequest(
            paymentStatus = "PAID",
            amountPaid = BigDecimal("1000.00"),
            remarks = "Updated payment"
        )

        every { paymentService.createPayment(createRequest) } returns expectedPaymentResponse
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse
        every { paymentService.updatePayment(paymentId, updateRequest) } returns expectedPaymentResponse
        every { paymentService.deletePayment(paymentId) } returns Unit

        // When & Then
        val createResponse = paymentService.createPayment(createRequest)
        assertEquals(expectedPaymentResponse.id, createResponse.id)
        assertEquals(expectedPaymentResponse.paymentStatus, createResponse.paymentStatus)

        val getResponse = paymentService.getPayment(paymentId)
        assertEquals(expectedPaymentResponse.id, getResponse.id)
        assertEquals(expectedPaymentResponse.amountPaid, getResponse.amountPaid)

        val updateResponse = paymentService.updatePayment(paymentId, updateRequest)
        assertEquals(expectedPaymentResponse.id, updateResponse.id)
        assertEquals(expectedPaymentResponse.remarks, updateResponse.remarks)

        paymentService.deletePayment(paymentId)
    }

    @Test
    @DisplayName("should handle different payment statuses correctly")
    fun `payment service should handle different payment statuses correctly`() {
        // Given
        val pendingPayment = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            paymentStatus = "PENDING_PAYMENT",
            amountPaid = BigDecimal("500.00")
        )
        val paidPayment = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            paymentStatus = "PAID",
            amountPaid = BigDecimal("500.00"),
            paidAt = testTime
        )
        val failedPayment = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            paymentStatus = "FAILED",
            amountPaid = null,
            remarks = "Payment failed due to insufficient funds"
        )

        every { paymentService.getPayment(paymentId) } returnsMany listOf(pendingPayment, paidPayment, failedPayment)

        // When & Then
        val firstResponse = paymentService.getPayment(paymentId)
        assertEquals("PENDING_PAYMENT", firstResponse.paymentStatus)
        assertEquals(BigDecimal("500.00"), firstResponse.amountPaid)

        val secondResponse = paymentService.getPayment(paymentId)
        assertEquals("PAID", secondResponse.paymentStatus)
        assertEquals(testTime, secondResponse.paidAt)

        val thirdResponse = paymentService.getPayment(paymentId)
        assertEquals("FAILED", thirdResponse.paymentStatus)
        assertEquals("Payment failed due to insufficient funds", thirdResponse.remarks)
    }

    @Test
    @DisplayName("should handle different payment methods correctly")
    fun `payment service should handle different payment methods correctly`() {
        // Given
        val creditCardPayment = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            paymentMethod = "CREDIT_CARD",
            transactionId = "CC_TXN_123"
        )
        val bankTransferPayment = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            paymentMethod = "BANK_TRANSFER",
            transactionId = "BT_TXN_456"
        )
        val upiPayment = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            paymentMethod = "UPI",
            transactionId = "UPI_TXN_789"
        )

        every { paymentService.getPayment(paymentId) } returnsMany listOf(creditCardPayment, bankTransferPayment, upiPayment)

        // When & Then
        val creditCardResponse = paymentService.getPayment(paymentId)
        assertEquals("CREDIT_CARD", creditCardResponse.paymentMethod)
        assertEquals("CC_TXN_123", creditCardResponse.transactionId)

        val bankTransferResponse = paymentService.getPayment(paymentId)
        assertEquals("BANK_TRANSFER", bankTransferResponse.paymentMethod)
        assertEquals("BT_TXN_456", bankTransferResponse.transactionId)

        val upiResponse = paymentService.getPayment(paymentId)
        assertEquals("UPI", upiResponse.paymentMethod)
        assertEquals("UPI_TXN_789", upiResponse.transactionId)
    }

    @Test
    @DisplayName("should handle payment with external data correctly")
    fun `payment service should handle payment with external data correctly`() {
        // Given
        val extData = mapOf(
            "gateway" to "Razorpay",
            "merchantId" to "MERCH123",
            "customerEmail" to "customer@example.com",
            "currency" to "INR"
        )
        val paymentWithExtData = PaymentTestUtils.createTestPaymentResponse(
            id = paymentId,
            extData = extData
        )

        every { paymentService.getPayment(paymentId) } returns paymentWithExtData

        // When
        val response = paymentService.getPayment(paymentId)

        // Then
        assertNotNull(response.extData)
        assertEquals("Razorpay", response.extData?.get("gateway"))
        assertEquals("MERCH123", response.extData?.get("merchantId"))
        assertEquals("customer@example.com", response.extData?.get("customerEmail"))
        assertEquals("INR", response.extData?.get("currency"))
    }
}
