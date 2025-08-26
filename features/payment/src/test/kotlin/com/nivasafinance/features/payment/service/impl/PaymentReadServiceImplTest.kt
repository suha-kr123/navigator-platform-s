package com.nivasafinance.features.payment.service.impl

import com.nivasafinance.features.payment.PaymentTestUtils.createTestPayment
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentData
import com.nivasafinance.features.payment.dto.PaymentData
import com.nivasafinance.features.payment.exception.PaymentNotFoundException
import com.nivasafinance.features.payment.repository.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PaymentReadServiceImpl Tests")
class PaymentReadServiceImplTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var paymentReadService: PaymentReadServiceImpl

    private val paymentId = UUID.randomUUID()
    private val payment = createTestPayment(id = paymentId)
    private val expectedPaymentData = createTestPaymentData(id = paymentId)

    @BeforeEach
    fun setup() {
        paymentReadService = PaymentReadServiceImpl(paymentRepository, messageSource)

        every { messageSource.getMessage(any(), any(), any()) } returns "Payment not found"
    }

    @Test
    @DisplayName("should return payment data when payment found")
    fun `getPaymentData should return payment data when payment found`() {
        every { paymentRepository.findById(paymentId) } returns Optional.of(payment)

        val result = paymentReadService.getPaymentData(paymentId)

        assertNotNull(result)
        assertEquals(expectedPaymentData.id, result.id)
        assertEquals(expectedPaymentData.paymentStatus, result.paymentStatus)
        assertEquals(expectedPaymentData.amountPaid, result.amountPaid)
        assertEquals(expectedPaymentData.paymentMethod, result.paymentMethod)
        assertEquals(expectedPaymentData.transactionId, result.transactionId)
        assertEquals(expectedPaymentData.remarks, result.remarks)
        assertEquals(expectedPaymentData.extData, result.extData)

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when payment not found")
    fun `getPaymentData should throw PaymentNotFoundException when payment not found`() {
        every { paymentRepository.findById(paymentId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Payment not found"

        assertThrows<PaymentNotFoundException> {
            paymentReadService.getPaymentData(paymentId)
        }

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }

    @Test
    @DisplayName("should handle payment with null values")
    fun `getPaymentData should handle payment with null values`() {
        val paymentWithNulls = createTestPayment(
            id = paymentId,
            amountPaid = null,
            paidAt = null,
            paymentMethod = null,
            transactionId = null,
            remarks = null,
            extData = null
        )
        
        every { paymentRepository.findById(paymentId) } returns Optional.of(paymentWithNulls)

        val result = paymentReadService.getPaymentData(paymentId)

        assertNotNull(result)
        assertEquals(paymentId, result.id)
        assertEquals(paymentWithNulls.paymentStatus, result.paymentStatus)
        assertEquals(null, result.amountPaid)
        assertEquals(null, result.paidAt)
        assertEquals(null, result.paymentMethod)
        assertEquals(null, result.transactionId)
        assertEquals(null, result.remarks)
        assertEquals(null, result.extData)

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
    }
}
