package com.nivasafinance.features.master.pincode.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "master_pincode")
data class Pincode(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "pincode", nullable = false, length = 10)
    val pincode: String,

    @Column(name = "area", nullable = false, length = 255)
    val area: String,

    @Column(name = "district", nullable = true, length = 255)
    val district: String? = null,

    @Column(name = "state", nullable = true, length = 255)
    val state: String? = null,

    @Column(name = "country", nullable = true, length = 255)
    val country: String? = null,

    @Column(name = "is_servicable", nullable = false)
    val isServicable: Boolean = false

) : AuditableEntity()
