package com.nivasafinance.features.lender.lenderoffice.entity

import audit.AuditableEntity
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "lender_office")
data class LenderOffice(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "key", nullable = false, length = 100)
    val key: String,

    @Column(name = "lender_key", nullable = false, length = 20)
    val lenderKey: String,

    @Column(name = "address_id", nullable = false)
    val addressId: UUID,

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    val status: LenderOfficeStatus

) : AuditableEntity()
