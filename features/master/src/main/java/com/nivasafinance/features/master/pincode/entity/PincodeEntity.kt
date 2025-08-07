package com.nivasafinance.features.master.pincode.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "master_pincode")
data class PincodeEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    val pincode: String,

    val area: String,

    val district: String? = null,

    val country: String? = null,

    @Column(name = "is_servicable")
    val isServicable: Boolean = false

) : AuditableEntity()
