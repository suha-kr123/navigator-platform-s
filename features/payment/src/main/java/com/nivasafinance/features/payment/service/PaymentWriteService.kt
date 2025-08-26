package com.nivasafinance.features.payment.service

import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentData
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import java.util.UUID

interface PaymentWriteService {
    fun createPaymentData(request: PaymentCreateRequest): PaymentData
    fun updatePaymentData(id: UUID, request: PaymentUpdateRequest): PaymentData
    fun deletePayment(id: UUID)
}
