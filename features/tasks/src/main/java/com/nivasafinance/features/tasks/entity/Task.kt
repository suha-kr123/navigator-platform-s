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

    @Column(name = "task_definition_key", nullable = false)
    val taskDefinitionKey: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "outcome")
    var outcome: String? = null,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "assigned_to")
    var assignedTo: String? = null,

    @Column(name = "due_at")
    var dueAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "rescheduled_at")
    var rescheduledAt: LocalDateTime? = null

) : AuditableEntity()
