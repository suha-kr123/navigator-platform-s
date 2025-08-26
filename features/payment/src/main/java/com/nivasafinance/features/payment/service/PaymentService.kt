package com.nivasafinance.features.payment.service

import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import java.util.UUID

interface PaymentService {
    fun getPayment(id: UUID): PaymentResponse
    fun createPayment(request: PaymentCreateRequest): PaymentResponse
    fun updatePayment(id: UUID, request: PaymentUpdateRequest): PaymentResponse
    fun deletePayment(id: UUID)
}
