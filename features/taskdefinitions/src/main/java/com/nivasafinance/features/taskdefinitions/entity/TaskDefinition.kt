package com.nivasafinance.features.taskdefinitions.entity

import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.TaskPriority
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import org.javers.core.metamodel.annotation.Id
import org.javers.spring.annotation.JaversSpringDataAuditable
import java.util.UUID

@Entity
@Table(name = "task_definitions")
@JaversSpringDataAuditable
@Suppress("ImportOrdering")
data class TaskDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "identifier", nullable = false)
    val identifier: String,

    @Column(name = "key", nullable = false, unique = true)
    val key: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "actions_group", nullable = false)
    val actionsGroup: String,

    @Column(name = "condition_on_action", columnDefinition = "jsonb")
    val conditionOnAction: String? = null,

    @Column(name = "tat_hours", columnDefinition = "jsonb")
    val tatHours: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_strategy", nullable = false)
    val assignmentStrategy: AssignmentStrategy,

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    val priority: TaskPriority,

    @Column(name = "possible_statuses", columnDefinition = "json")
    val possibleStatuses: String? = null
)
