package com.nivasafinance.features.tasks.entity

import audit.AuditableEntity
import com.nivasafinance.features.tasks.enum.TaskStatus
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_data", columnDefinition = "jsonb")
    val taskData: String? = null,

    @Column(name = "assigned_to")
    val assignedTo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: TaskStatus,

    @Column(name = "outcome", nullable = false)
    val outcome: String,

    @Column(name = "due_at")
    val dueAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null,

    @Column(name = "rescheduled_at")
    val rescheduledAt: LocalDateTime? = null

) : AuditableEntity()
