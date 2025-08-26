package com.nivasafinance.features.payment.service

import com.nivasafinance.features.payment.dto.PaymentData
import java.util.UUID

interface PaymentReadService {
    fun getPaymentData(id: UUID): PaymentData
}
