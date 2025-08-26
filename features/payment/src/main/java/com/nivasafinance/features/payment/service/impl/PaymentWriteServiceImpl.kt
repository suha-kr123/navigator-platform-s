package com.nivasafinance.features.payment.service.impl

import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentData
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.entity.Payment
import com.nivasafinance.features.payment.enum.PaymentStatus
import com.nivasafinance.features.payment.exception.PaymentNotFoundException
import com.nivasafinance.features.payment.repository.PaymentRepository
import com.nivasafinance.features.payment.service.PaymentWriteService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PaymentWriteServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val messageSource: MessageSource
) : PaymentWriteService {

    @Transactional
    override fun createPaymentData(request: PaymentCreateRequest): PaymentData {
        val payment = Payment(
            paymentStatus = request.paymentStatus?.let { PaymentStatus.valueOf(it) } ?: PaymentStatus.PENDING_PAYMENT,
            amountPaid = request.amountPaid,
            paidAt = request.paidAt,
            paymentMethod = request.paymentMethod,
            transactionId = request.transactionId,
            remarks = request.remarks,
            extData = request.extData
        )
        val savedPayment = paymentRepository.save(payment)
        return PaymentData.fromEntity(savedPayment)
    }

    @Transactional
    override fun updatePaymentData(id: UUID, request: PaymentUpdateRequest): PaymentData {
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException(id, messageSource) }

        val updatedPayment = Payment(
            id = payment.id,
            paymentStatus = request.paymentStatus?.let { PaymentStatus.valueOf(it) } ?: payment.paymentStatus,
            amountPaid = request.amountPaid ?: payment.amountPaid,
            paidAt = request.paidAt ?: payment.paidAt,
            paymentMethod = request.paymentMethod ?: payment.paymentMethod,
            transactionId = request.transactionId ?: payment.transactionId,
            remarks = request.remarks ?: payment.remarks,
            extData = request.extData ?: payment.extData
        )

        val savedPayment = paymentRepository.save(updatedPayment)
        return PaymentData.fromEntity(savedPayment)
    }

    @Transactional
    override fun deletePayment(id: UUID) {
        if (!paymentRepository.existsById(id)) {
            throw PaymentNotFoundException(id, messageSource)
        }
        paymentRepository.deleteById(id)
    }
}
