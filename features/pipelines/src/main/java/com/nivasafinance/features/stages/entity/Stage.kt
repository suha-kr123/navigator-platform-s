package com.nivasafinance.features.stages.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

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
