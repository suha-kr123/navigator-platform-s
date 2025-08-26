package com.nivasafinance.features.payment.controller

import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentCreateRequest
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentResponse
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentUpdateRequest
import com.nivasafinance.features.payment.service.PaymentService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    private val paymentService = mockk<PaymentService>()
    private lateinit var paymentController: PaymentController

    private val paymentId = UUID.randomUUID()
    private val expectedResponse = createTestPaymentResponse(id = paymentId)

    @BeforeEach
    fun setup() {
        paymentController = PaymentController(paymentService)
    }

    @Test
    @DisplayName("should return payment when found")
    fun `getPayment should return payment when found`() {
        every { paymentService.getPayment(paymentId) } returns expectedResponse

        val result = paymentController.getPayment(paymentId)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.paymentStatus, result.body?.paymentStatus)
        assertEquals(expectedResponse.amountPaid, result.body?.amountPaid)
        assertEquals(expectedResponse.paidAt, result.body?.paidAt)
        assertEquals(expectedResponse.paymentMethod, result.body?.paymentMethod)
        assertEquals(expectedResponse.transactionId, result.body?.transactionId)
        assertEquals(expectedResponse.remarks, result.body?.remarks)
        assertEquals(expectedResponse.extData, result.body?.extData)

        verify(exactly = 1) { paymentService.getPayment(paymentId) }
    }

    @Test
    @DisplayName("should create payment successfully")
    fun `createPayment should create payment successfully`() {
        val request = createTestPaymentCreateRequest()
        every { paymentService.createPayment(request) } returns expectedResponse

        val result = paymentController.createPayment(request)

        assertNotNull(result)
        assertEquals(201, result.statusCode.value())
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.paymentStatus, result.body?.paymentStatus)
        assertEquals(expectedResponse.amountPaid, result.body?.amountPaid)
        assertEquals(expectedResponse.paidAt, result.body?.paidAt)
        assertEquals(expectedResponse.paymentMethod, result.body?.paymentMethod)
        assertEquals(expectedResponse.transactionId, result.body?.transactionId)
        assertEquals(expectedResponse.remarks, result.body?.remarks)
        assertEquals(expectedResponse.extData, result.body?.extData)

        verify(exactly = 1) { paymentService.createPayment(request) }
    }

    @Test
    @DisplayName("should update payment successfully")
    fun `updatePayment should update payment successfully`() {
        val request = createTestPaymentUpdateRequest()
        val updatedResponse = createTestPaymentResponse(
            id = paymentId,
            paymentStatus = "PAID",
            remarks = "Updated payment"
        )
        every { paymentService.updatePayment(paymentId, request) } returns updatedResponse

        val result = paymentController.updatePayment(paymentId, request)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(updatedResponse.id, result.body?.id)
        assertEquals(updatedResponse.paymentStatus, result.body?.paymentStatus)
        assertEquals(updatedResponse.amountPaid, result.body?.amountPaid)
        assertEquals(updatedResponse.paidAt, result.body?.paidAt)
        assertEquals(updatedResponse.paymentMethod, result.body?.paymentMethod)
        assertEquals(updatedResponse.transactionId, result.body?.transactionId)
        assertEquals(updatedResponse.remarks, result.body?.remarks)
        assertEquals(updatedResponse.extData, result.body?.extData)

        verify(exactly = 1) { paymentService.updatePayment(paymentId, request) }
    }

    @Test
    @DisplayName("should delete payment successfully")
    fun `deletePayment should delete payment successfully`() {
        every { paymentService.deletePayment(paymentId) } returns Unit

        val result = paymentController.deletePayment(paymentId)

        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { paymentService.deletePayment(paymentId) }
    }
}
