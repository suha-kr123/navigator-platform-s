package com.nivasafinance.features.taskdefinitions.entity

import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.Priority
import com.nivasafinance.features.taskdefinitions.enum.TaskStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*
import audit.AuditableEntity

@Entity
@Table(name = "task_definitions")
data class TaskDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "type", nullable = false)
    val type: String,

    @Column(name = "key", unique = true, nullable = false)
    val key: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "actions_group", nullable = false)
    val actionsGroup: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_on_action", columnDefinition = "jsonb")
    val conditionOnAction: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tat_hours", columnDefinition = "jsonb")
    val tatHours: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_strategy", nullable = false)
    val assignmentStrategy: AssignmentStrategy,

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    val priority: Priority,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "possible_statuses", columnDefinition = "jsonb")
    val possibleStatuses: String? = null
    
) : AuditableEntity()
