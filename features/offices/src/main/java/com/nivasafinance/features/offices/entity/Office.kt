package com.nivasafinance.features.offices.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "offices")
class Office(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "key", nullable = false)
    var key: String = "",

    @Column(name = "code", nullable = false)
    var code: String = "",

    @Column(name = "address_id")
    var addressId: UUID? = null,
) : AuditableEntity()
