package com.nivasafinance.features.tasks.entity

import audit.AuditableEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "task_definition_key", nullable = false, unique = true)
    val taskDefinitionKey: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "entity_type", nullable = false)
    val entityType: String,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_data", columnDefinition = "jsonb")
    var taskData: Map<String, Any>? = null,

    @Column(name = "outcome", nullable = false)
    var outcome: String,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "assigned_to")
    var assignedTo: String? = null,

    @Column(name = "due_at")
    var dueAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "rescheduled_at")
    var rescheduledAt: LocalDateTime? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "note_ids", columnDefinition = "jsonb")
    var noteIds: List<UUID>? = null

) : AuditableEntity()
