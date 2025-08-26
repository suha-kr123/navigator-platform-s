package com.nivasafinance.features.payment.service.impl

import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentCreateRequest
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentData
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentResponse
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentUpdateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.enum.PaymentStatus
import com.nivasafinance.features.payment.service.PaymentReadService
import com.nivasafinance.features.payment.service.PaymentWriteService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    private val paymentReadService = mockk<PaymentReadService>()
    private val paymentWriteService = mockk<PaymentWriteService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var paymentService: PaymentServiceImpl

    private val paymentId = UUID.randomUUID()
    private val paymentData = createTestPaymentData(id = paymentId)
    private val expectedResponse = createTestPaymentResponse(id = paymentId)

    @BeforeEach
    fun setup() {
        paymentService = PaymentServiceImpl(paymentReadService, paymentWriteService)

        val modelMapperField = paymentService.javaClass.superclass.getDeclaredField("modelMapper")
        modelMapperField.isAccessible = true
        modelMapperField.set(paymentService, modelMapper)

        val messageSourceField = paymentService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(paymentService, messageSource)
    }

    @Test
    @DisplayName("should get payment successfully")
    fun `getPayment should return payment response successfully`() {
        every { paymentReadService.getPaymentData(paymentId) } returns paymentData

        val result = paymentService.getPayment(paymentId)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.paymentStatus, result.paymentStatus)
        assertEquals(expectedResponse.amountPaid, result.amountPaid)
        assertEquals(expectedResponse.paidAt, result.paidAt)
        assertEquals(expectedResponse.paymentMethod, result.paymentMethod)
        assertEquals(expectedResponse.transactionId, result.transactionId)
        assertEquals(expectedResponse.remarks, result.remarks)
        assertEquals(expectedResponse.extData, result.extData)

        verify(exactly = 1) { paymentReadService.getPaymentData(paymentId) }
    }

    @Test
    @DisplayName("should create payment successfully")
    fun `createPayment should create payment successfully`() {
        val request = createTestPaymentCreateRequest()
        val createdPaymentData = createTestPaymentData(id = paymentId)
        val expectedCreatedResponse = createTestPaymentResponse(id = paymentId)

        every { paymentWriteService.createPaymentData(request) } returns createdPaymentData

        val result = paymentService.createPayment(request)

        assertNotNull(result)
        assertEquals(expectedCreatedResponse.id, result.id)
        assertEquals(expectedCreatedResponse.paymentStatus, result.paymentStatus)
        assertEquals(expectedCreatedResponse.amountPaid, result.amountPaid)
        assertEquals(expectedCreatedResponse.paidAt, result.paidAt)
        assertEquals(expectedCreatedResponse.paymentMethod, result.paymentMethod)
        assertEquals(expectedCreatedResponse.transactionId, result.transactionId)
        assertEquals(expectedCreatedResponse.remarks, result.remarks)
        assertEquals(expectedCreatedResponse.extData, result.extData)

        verify(exactly = 1) { paymentWriteService.createPaymentData(request) }
    }

    @Test
    @DisplayName("should update payment successfully")
    fun `updatePayment should update payment successfully`() {
        val request = createTestPaymentUpdateRequest()
        val updatedPaymentData = createTestPaymentData(
            id = paymentId,
            paymentStatus = PaymentStatus.PAID,
            remarks = "Updated payment"
        )
        val expectedUpdatedResponse = createTestPaymentResponse(
            id = paymentId,
            paymentStatus = "PAID",
            remarks = "Updated payment"
        )

        every { paymentWriteService.updatePaymentData(paymentId, request) } returns updatedPaymentData

        val result = paymentService.updatePayment(paymentId, request)

        assertNotNull(result)
        assertEquals(expectedUpdatedResponse.id, result.id)
        assertEquals(expectedUpdatedResponse.paymentStatus, result.paymentStatus)
        assertEquals(expectedUpdatedResponse.amountPaid, result.amountPaid)
        assertEquals(expectedUpdatedResponse.paidAt, result.paidAt)
        assertEquals(expectedUpdatedResponse.paymentMethod, result.paymentMethod)
        assertEquals(expectedUpdatedResponse.transactionId, result.transactionId)
        assertEquals(expectedUpdatedResponse.remarks, result.remarks)
        assertEquals(expectedUpdatedResponse.extData, result.extData)

        verify(exactly = 1) { paymentWriteService.updatePaymentData(paymentId, request) }
    }

    @Test
    @DisplayName("should delete payment successfully")
    fun `deletePayment should delete payment successfully`() {
        every { paymentWriteService.deletePayment(paymentId) } returns Unit

        paymentService.deletePayment(paymentId)

        verify(exactly = 1) { paymentWriteService.deletePayment(paymentId) }
    }

    @Test
    @DisplayName("should handle payment with complex extData")
    fun `should handle payment with complex extData`() {
        val complexExtData = mapOf(
            "gateway" to "stripe",
            "fee" to BigDecimal("2.50"),
            "metadata" to mapOf("order_id" to "ORD123", "customer_id" to "CUST456")
        )
        val paymentDataWithComplexExt = createTestPaymentData(
            id = paymentId,
            extData = complexExtData
        )

        every { paymentReadService.getPaymentData(paymentId) } returns paymentDataWithComplexExt

        val result = paymentService.getPayment(paymentId)

        assertNotNull(result)
        assertEquals(complexExtData, result.extData)

        verify(exactly = 1) { paymentReadService.getPaymentData(paymentId) }
    }

    @Test
    @DisplayName("should handle different payment statuses")
    fun `should handle different payment statuses`() {
        val statuses = listOf(
            PaymentStatus.NEW_LEAD,
            PaymentStatus.QUALIFIED,
            PaymentStatus.UNQUALIFIED,
            PaymentStatus.PENDING_PAYMENT,
            PaymentStatus.PAID,
            PaymentStatus.FAILED,
            PaymentStatus.CANCELLED,
            PaymentStatus.REFUNDED
        )

        statuses.forEach { status ->
            val paymentDataWithStatus = createTestPaymentData(id = paymentId, paymentStatus = status)
            every { paymentReadService.getPaymentData(paymentId) } returns paymentDataWithStatus

            val result = paymentService.getPayment(paymentId)

            assertNotNull(result)
            assertEquals(status.name, result.paymentStatus)
        }

        verify(exactly = statuses.size) { paymentReadService.getPaymentData(paymentId) }
    }
}
