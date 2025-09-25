package com.nivasafinance.features.stages.entity

import audit.AuditableEntity
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

    @Column(name = "outcome", nullable = false)
    val outcome: String?,

    @Column(name = "assigned_to")
    val assignedTo: String? = null

) : AuditableEntity()
