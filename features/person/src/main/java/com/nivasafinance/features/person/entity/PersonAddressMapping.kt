package com.nivasafinance.features.person.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "person_address_mapping")
class PersonAddressMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "person_id")
    var personId: UUID? = null,

    @Column(name = "address_id")
    var addressId: UUID? = null,

    @Column(name = "address_type")
    var addressType: String? = null // HOME, OFFICE, PERMANENT, etc.
) : AuditableEntity()
