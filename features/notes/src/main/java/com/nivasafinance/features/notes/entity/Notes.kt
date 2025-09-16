package com.nivasafinance.features.notes.entity

import annotations.NoArg
import audit.AuditableEntity
import com.nivasafinance.features.notes.enum.EntityType
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
@Table(name = "notes")
@NoArg
data class Notes(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "notes", columnDefinition = "TEXT", nullable = false)
    val notes: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID

) : AuditableEntity()
