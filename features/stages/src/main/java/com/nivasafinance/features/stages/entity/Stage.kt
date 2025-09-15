package com.nivasafinance.features.stages.entity

import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.enum.Outcome
import com.nivasafinance.features.stages.enum.Status
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*
import audit.AuditableEntity

@Entity
@Table(name = "stages")
data class Stage(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    val outcome: Outcome,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: Status,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tasks", columnDefinition = "jsonb")
    val tasks: String? = null,

    @Column(name = "assigned_to")
    val assignedTo: String? = null

): AuditableEntity()
