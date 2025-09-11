package com.nivasafinance.features.advisorleadmapping.client

import java.util.UUID

interface PaymentClient {
    fun createPayment(request: PaymentCreateRequest): PaymentResponse
    fun getPayment(id: UUID): PaymentResponse
    fun updatePayment(id: UUID, request: PaymentUpdateRequest): PaymentResponse
    fun deletePayment(id: UUID)
}
