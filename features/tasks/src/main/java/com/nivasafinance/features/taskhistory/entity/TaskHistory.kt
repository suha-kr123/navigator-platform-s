package com.nivasafinance.features.taskhistory.entity

import com.nivasafinance.features.taskhistory.enum.TaskEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity for tracking task history events.
 * Uses enums to ensure data integrity for event types and field names.
 */
@Entity
@Table(name = "task_history")
data class TaskHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "task_id", nullable = false)
    val taskId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    val eventType: TaskEventType,

    @Column(name = "old_value", columnDefinition = "TEXT")
    val oldValue: String? = null,

    @Column(name = "new_value", columnDefinition = "TEXT")
    val newValue: String? = null,

    @Column(name = "changed_by", nullable = false)
    val changedBy: String,

    @Column(name = "changed_at", nullable = false)
    val changedAt: LocalDateTime
)
