package com.nivasafinance.features.offices.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

    @Column(name = "parent_id")
    var parentId: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    var parent: Office? = null,
) : AuditableEntity()
