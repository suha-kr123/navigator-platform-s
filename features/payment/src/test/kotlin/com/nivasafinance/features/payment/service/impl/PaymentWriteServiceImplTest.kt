package com.nivasafinance.features.payment.service.impl

import com.nivasafinance.features.payment.PaymentTestUtils.createTestPayment
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentCreateRequest
import com.nivasafinance.features.payment.PaymentTestUtils.createTestPaymentUpdateRequest
import com.nivasafinance.features.payment.entity.Payment
import com.nivasafinance.features.payment.enum.PaymentStatus
import com.nivasafinance.features.payment.exception.PaymentNotFoundException
import com.nivasafinance.features.payment.repository.PaymentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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

@DisplayName("PaymentWriteServiceImpl Tests")
class PaymentWriteServiceImplTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var paymentWriteService: PaymentWriteServiceImpl

    private val paymentId = UUID.randomUUID()
    private val existingPayment = createTestPayment(id = paymentId)

    @BeforeEach
    fun setup() {
        paymentWriteService = PaymentWriteServiceImpl(paymentRepository, messageSource)

        every { messageSource.getMessage(any(), any(), any()) } returns "Payment not found"
    }

    @Test
    @DisplayName("should create payment with all fields")
    fun `createPaymentData should create payment with all fields`() {
        val request = createTestPaymentCreateRequest()
        val savedPayment = createTestPayment(id = paymentId)
        val paymentSlot = slot<Payment>()

        every { paymentRepository.save(capture(paymentSlot)) } returns savedPayment

        val result = paymentWriteService.createPaymentData(request)

        assertNotNull(result)
        assertEquals(paymentId, result.id)
        assertEquals(PaymentStatus.PENDING_PAYMENT, result.paymentStatus)
        assertEquals(request.amountPaid, result.amountPaid)
        assertEquals(request.paidAt, result.paidAt)
        assertEquals(request.paymentMethod, result.paymentMethod)
        assertEquals(request.transactionId, result.transactionId)
        assertEquals(request.remarks, result.remarks)
        assertEquals(request.extData, result.extData)

        verify(exactly = 1) { paymentRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assertEquals(PaymentStatus.PENDING_PAYMENT, capturedPayment.paymentStatus)
        assertEquals(request.amountPaid, capturedPayment.amountPaid)
        assertEquals(request.paidAt, capturedPayment.paidAt)
        assertEquals(request.paymentMethod, capturedPayment.paymentMethod)
        assertEquals(request.transactionId, capturedPayment.transactionId)
        assertEquals(request.remarks, capturedPayment.remarks)
        assertEquals(request.extData, capturedPayment.extData)
    }

    @Test
    @DisplayName("should create payment with custom status")
    fun `createPaymentData should create payment with custom status`() {
        val request = createTestPaymentCreateRequest(paymentStatus = "PAID")
        val savedPayment = createTestPayment(id = paymentId, paymentStatus = PaymentStatus.PAID)
        val paymentSlot = slot<Payment>()

        every { paymentRepository.save(capture(paymentSlot)) } returns savedPayment

        val result = paymentWriteService.createPaymentData(request)

        assertNotNull(result)
        assertEquals(PaymentStatus.PAID, result.paymentStatus)

        verify(exactly = 1) { paymentRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assertEquals(PaymentStatus.PAID, capturedPayment.paymentStatus)
    }

    @Test
    @DisplayName("should create payment with null status using default")
    fun `createPaymentData should create payment with null status using default`() {
        val request = createTestPaymentCreateRequest(paymentStatus = null)
        val savedPayment = createTestPayment(id = paymentId)
        val paymentSlot = slot<Payment>()

        every { paymentRepository.save(capture(paymentSlot)) } returns savedPayment

        val result = paymentWriteService.createPaymentData(request)

        assertNotNull(result)
        assertEquals(PaymentStatus.PENDING_PAYMENT, result.paymentStatus)

        verify(exactly = 1) { paymentRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assertEquals(PaymentStatus.PENDING_PAYMENT, capturedPayment.paymentStatus)
    }

    @Test
    @DisplayName("should create payment with minimal data")
    fun `createPaymentData should create payment with minimal data`() {
        val request = createTestPaymentCreateRequest(
            paymentStatus = null,
            amountPaid = null,
            paidAt = null,
            paymentMethod = null,
            transactionId = null,
            remarks = null,
            extData = null
        )
        val savedPayment = createTestPayment(
            id = paymentId,
            amountPaid = null,
            paidAt = null,
            paymentMethod = null,
            transactionId = null,
            remarks = null,
            extData = null
        )
        val paymentSlot = slot<Payment>()

        every { paymentRepository.save(capture(paymentSlot)) } returns savedPayment

        val result = paymentWriteService.createPaymentData(request)

        assertNotNull(result)
        assertEquals(paymentId, result.id)
        assertEquals(PaymentStatus.PENDING_PAYMENT, result.paymentStatus)
        assertEquals(null, result.amountPaid)
        assertEquals(null, result.paidAt)
        assertEquals(null, result.paymentMethod)
        assertEquals(null, result.transactionId)
        assertEquals(null, result.remarks)
        assertEquals(null, result.extData)

        verify(exactly = 1) { paymentRepository.save(any()) }
    }

    @Test
    @DisplayName("should update payment with all fields")
    fun `updatePaymentData should update payment with all fields`() {
        val request = createTestPaymentUpdateRequest()
        val updatedPayment = createTestPayment(
            id = paymentId,
            paymentStatus = PaymentStatus.PAID,
            remarks = "Updated payment",
            extData = request.extData
        )
        val paymentSlot = slot<Payment>()

        every { paymentRepository.findById(paymentId) } returns Optional.of(existingPayment)
        every { paymentRepository.save(capture(paymentSlot)) } returns updatedPayment

        val result = paymentWriteService.updatePaymentData(paymentId, request)

        assertNotNull(result)
        assertEquals(paymentId, result.id)
        assertEquals(PaymentStatus.PAID, result.paymentStatus)
        assertEquals(request.amountPaid, result.amountPaid)
        assertEquals(request.paidAt, result.paidAt)
        assertEquals(request.paymentMethod, result.paymentMethod)
        assertEquals(request.transactionId, result.transactionId)
        assertEquals(request.remarks, result.remarks)
        assertEquals(request.extData, result.extData)

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assertEquals(paymentId, capturedPayment.id)
        assertEquals(PaymentStatus.PAID, capturedPayment.paymentStatus)
        assertEquals(request.amountPaid, capturedPayment.amountPaid)
        assertEquals(request.paidAt, capturedPayment.paidAt)
        assertEquals(request.paymentMethod, capturedPayment.paymentMethod)
        assertEquals(request.transactionId, capturedPayment.transactionId)
        assertEquals(request.remarks, capturedPayment.remarks)
        assertEquals(request.extData, capturedPayment.extData)
    }

    @Test
    @DisplayName("should update payment with partial data")
    fun `updatePaymentData should update payment with partial data`() {
        val request = createTestPaymentUpdateRequest(
            paymentStatus = "PAID",
            amountPaid = null,
            paidAt = null,
            paymentMethod = null,
            transactionId = null,
            remarks = null,
            extData = null
        )
        val updatedPayment = createTestPayment(
            id = paymentId,
            paymentStatus = PaymentStatus.PAID
        )
        val paymentSlot = slot<Payment>()

        every { paymentRepository.findById(paymentId) } returns Optional.of(existingPayment)
        every { paymentRepository.save(capture(paymentSlot)) } returns updatedPayment

        val result = paymentWriteService.updatePaymentData(paymentId, request)

        assertNotNull(result)
        assertEquals(PaymentStatus.PAID, result.paymentStatus)

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { paymentRepository.save(any()) }

        val capturedPayment = paymentSlot.captured
        assertEquals(PaymentStatus.PAID, capturedPayment.paymentStatus)
        assertEquals(existingPayment.amountPaid, capturedPayment.amountPaid)
        assertEquals(existingPayment.paidAt, capturedPayment.paidAt)
        assertEquals(existingPayment.paymentMethod, capturedPayment.paymentMethod)
        assertEquals(existingPayment.transactionId, capturedPayment.transactionId)
        assertEquals(existingPayment.remarks, capturedPayment.remarks)
        assertEquals(existingPayment.extData, capturedPayment.extData)
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when updating non-existent payment")
    fun `updatePaymentData should throw PaymentNotFoundException when updating non-existent payment`() {
        val request = createTestPaymentUpdateRequest()

        every { paymentRepository.findById(paymentId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Payment not found"

        assertThrows<PaymentNotFoundException> {
            paymentWriteService.updatePaymentData(paymentId, request)
        }

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        verify(exactly = 0) { paymentRepository.save(any()) }
    }

    @Test
    @DisplayName("should delete existing payment")
    fun `deletePayment should delete existing payment`() {
        every { paymentRepository.existsById(paymentId) } returns true
        every { paymentRepository.deleteById(paymentId) } returns Unit

        paymentWriteService.deletePayment(paymentId)

        verify(exactly = 1) { paymentRepository.existsById(paymentId) }
        verify(exactly = 1) { paymentRepository.deleteById(paymentId) }
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when deleting non-existent payment")
    fun `deletePayment should throw PaymentNotFoundException when deleting non-existent payment`() {
        every { paymentRepository.existsById(paymentId) } returns false
        every { messageSource.getMessage(any(), any(), any()) } returns "Payment not found"

        assertThrows<PaymentNotFoundException> {
            paymentWriteService.deletePayment(paymentId)
        }

        verify(exactly = 1) { paymentRepository.existsById(paymentId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        verify(exactly = 0) { paymentRepository.deleteById(any()) }
    }
}
