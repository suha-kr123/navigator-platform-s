package com.nivasafinance.features.payment.entity

import audit.AuditableEntity
import com.nivasafinance.features.payment.enum.PaymentStatus
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING_PAYMENT,

    @Column(name = "amount_paid", precision = 19, scale = 2)
    val amountPaid: BigDecimal? = null,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null,

    @Column(name = "payment_method")
    val paymentMethod: String? = null,

    @Column(name = "transaction_id")
    val transactionId: String? = null,

    @Column(name = "remarks")
    val remarks: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null
) : AuditableEntity()
