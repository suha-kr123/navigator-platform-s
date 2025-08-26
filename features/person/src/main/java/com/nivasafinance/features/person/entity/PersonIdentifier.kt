package com.nivasafinance.features.person.entity

import audit.AuditableEntity
import com.nivasafinance.features.person.enum.IdentifierType
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
@Table(name = "person_identifiers")
class PersonIdentifier(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "person_id")
    var personId: UUID,

    @Column(name = "identifier", length = 100)
    var identifier: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    var type: IdentifierType
) : AuditableEntity()
