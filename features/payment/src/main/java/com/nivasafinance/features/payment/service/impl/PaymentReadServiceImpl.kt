package com.nivasafinance.features.payment.service.impl

import com.nivasafinance.features.payment.dto.PaymentData
import com.nivasafinance.features.payment.exception.PaymentNotFoundException
import com.nivasafinance.features.payment.repository.PaymentRepository
import com.nivasafinance.features.payment.service.PaymentReadService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PaymentReadServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val messageSource: MessageSource
) : PaymentReadService {

    override fun getPaymentData(id: UUID): PaymentData {
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException(id, messageSource) }
        return PaymentData.fromEntity(payment)
    }
}
