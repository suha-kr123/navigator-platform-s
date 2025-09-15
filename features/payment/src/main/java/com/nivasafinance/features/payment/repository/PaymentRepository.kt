package com.nivasafinance.features.payment.repository

import com.nivasafinance.features.payment.entity.Payment
import com.nivasafinance.features.payment.enum.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {
}
