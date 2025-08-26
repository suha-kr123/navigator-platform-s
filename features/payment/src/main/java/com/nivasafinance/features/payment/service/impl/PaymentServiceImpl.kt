package com.nivasafinance.features.payment.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentData
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.service.PaymentReadService
import com.nivasafinance.features.payment.service.PaymentService
import com.nivasafinance.features.payment.service.PaymentWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PaymentServiceImpl(
    private val paymentReadService: PaymentReadService,
    private val paymentWriteService: PaymentWriteService
) : PaymentService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "payment"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getPayment(id: UUID): PaymentResponse {
        val paymentData = paymentReadService.getPaymentData(id)
        return mapDataToResponse(paymentData)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createPayment(request: PaymentCreateRequest): PaymentResponse {
        val paymentData = paymentWriteService.createPaymentData(request)
        return mapDataToResponse(paymentData)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    override fun updatePayment(id: UUID, request: PaymentUpdateRequest): PaymentResponse {
        val paymentData = paymentWriteService.updatePaymentData(id, request)
        return mapDataToResponse(paymentData)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    override fun deletePayment(id: UUID) {
        paymentWriteService.deletePayment(id)
    }

    private fun mapDataToResponse(paymentData: PaymentData): PaymentResponse {
        return PaymentResponse(
            id = paymentData.id!!,
            paymentStatus = paymentData.paymentStatus.name,
            amountPaid = paymentData.amountPaid,
            paidAt = paymentData.paidAt,
            paymentMethod = paymentData.paymentMethod,
            transactionId = paymentData.transactionId,
            remarks = paymentData.remarks,
            extData = paymentData.extData
        )
    }
}
