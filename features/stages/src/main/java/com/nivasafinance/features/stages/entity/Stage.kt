package com.nivasafinance.features.stages.entity

import audit.AuditableEntity
import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.enum.Status
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "stages")
data class Stage(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "stage_definition_key", nullable = false)
    val stageDefinitionKey: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    @Column(name = "outcome", nullable = false)
    val outcome: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: Status,

    @Column(name = "assigned_to")
    val assignedTo: String? = null

) : AuditableEntity()
