package com.nivasafinance.features.tasks.entity

import audit.AuditableEntity
import com.nivasafinance.features.tasks.enum.TaskStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "lead_tasks")
data class LeadTask(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "task_key", nullable = false)
    val taskKey: String,

    @Column(name = "lead_id", nullable = false)
    val leadId: UUID,

    @Column(name = "stage", nullable = false)
    val stage: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_data", columnDefinition = "jsonb")
    var taskData: Map<String, Any>? = null,

    @Column(name = "assigned_to")
    var assignedTo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TaskStatus,

    @Column(name = "due_at")
    var dueAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "rescheduled_at")
    var rescheduledAt: LocalDateTime? = null
) : AuditableEntity()
