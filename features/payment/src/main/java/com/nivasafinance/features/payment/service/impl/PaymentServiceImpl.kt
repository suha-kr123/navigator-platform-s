package com.nivasafinance.features.payment.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.entity.Payment
import com.nivasafinance.features.payment.enum.PaymentStatus
import com.nivasafinance.features.payment.exception.PaymentNotFoundException
import com.nivasafinance.features.payment.repository.PaymentRepository
import com.nivasafinance.features.payment.service.PaymentService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "paymentCacheManager")
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository
) : PaymentService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "payment"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getPayment(id: UUID): PaymentResponse {
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException(id, messageSource) }
        return mapEntityToResponse(payment)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createPayment(request: PaymentCreateRequest): PaymentResponse {
        val payment = Payment(
            entityId = request.entityId,
            entityType = request.entityType,
            paymentType = request.paymentType,
            paymentStatus = if (request.paymentStatus != null) {
                PaymentStatus.valueOf(
                    request.paymentStatus
                )
            } else {
                PaymentStatus.PENDING_PAYMENT
            },
            amountPaid = request.amountPaid,
            paidAt = request.paidAt,
            paymentMethod = request.paymentMethod,
            transactionId = request.transactionId,
            remarks = request.remarks,
            extData = request.extData
        )

        val savedPayment = paymentRepository.save(payment)
        return mapEntityToResponse(savedPayment)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    override fun updatePayment(id: UUID, request: PaymentUpdateRequest): PaymentResponse {
        val existingPayment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException(id, messageSource) }

        val updatedPayment = Payment(
            id = existingPayment.id,
            entityId = existingPayment.entityId,
            entityType = existingPayment.entityType,
            paymentType = existingPayment.paymentType,
            paymentStatus = if (request.paymentStatus != null) {
                PaymentStatus.valueOf(
                    request.paymentStatus
                )
            } else {
                existingPayment.paymentStatus
            },
            amountPaid = request.amountPaid ?: existingPayment.amountPaid,
            paidAt = request.paidAt ?: existingPayment.paidAt,
            paymentMethod = request.paymentMethod ?: existingPayment.paymentMethod,
            transactionId = request.transactionId ?: existingPayment.transactionId,
            remarks = request.remarks ?: existingPayment.remarks,
            extData = request.extData ?: existingPayment.extData
        )

        val savedPayment = paymentRepository.save(updatedPayment)
        return mapEntityToResponse(savedPayment)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    override fun deletePayment(id: UUID) {
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException(id, messageSource) }
        paymentRepository.deleteById(payment.id!!)
    }

    private fun mapEntityToResponse(payment: Payment): PaymentResponse {
        return PaymentResponse(
            id = payment.id!!,
            entityId = payment.entityId,
            entityType = payment.entityType,
            paymentType = payment.paymentType,
            paymentStatus = payment.paymentStatus.name,
            amountPaid = payment.amountPaid,
            paidAt = payment.paidAt,
            paymentMethod = payment.paymentMethod,
            transactionId = payment.transactionId,
            remarks = payment.remarks,
            extData = payment.extData
        )
    }
}
