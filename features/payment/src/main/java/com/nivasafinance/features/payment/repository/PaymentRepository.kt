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

    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): List<Payment>

    fun findByEntityIdAndEntityTypeAndPaymentType(entityId: UUID, entityType: String, paymentType: String): List<Payment>

    fun findByEntityIdAndEntityTypeAndPaymentStatus(entityId: UUID, entityType: String, paymentStatus: PaymentStatus): List<Payment>

    fun existsByEntityIdAndEntityTypeAndPaymentType(entityId: UUID, entityType: String, paymentType: String): Boolean

    @Query("SELECT p FROM Payment p WHERE p.entityType = :entityType AND p.paymentType = :paymentType")
    fun findByEntityTypeAndPaymentType(
        @Param("entityType") entityType: String,
        @Param("paymentType") paymentType: String
    ): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.entityType = :entityType AND p.paymentStatus = :paymentStatus")
    fun findByEntityTypeAndPaymentStatus(
        @Param("entityType") entityType: String,
        @Param("paymentStatus") paymentStatus: PaymentStatus
    ): List<Payment>

    fun findByTransactionId(transactionId: String): Payment?
}
