package com.nivasafinance.features.lender.lender.entity

import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.lender.lender.enum.LenderStatus
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
@Table(name = "n_lender")
data class Lender(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "key", nullable = false, length = 20)
    val key: String,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    val status: LenderStatus

) : AuditableEntity()
