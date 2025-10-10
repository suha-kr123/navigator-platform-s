package com.nivasafinance.features.tasks.entity

import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.tasks.enum.TaskStatus
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

@Entity
@Table(name = "tasks")
class Task : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null

    @Column(name = "task_definition_key", nullable = false)
    var taskDefinitionKey: String = ""

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "description", nullable = true)
    var description: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TaskStatus = TaskStatus.TODO

    @Column(name = "outcome", nullable = true)
    var outcome: String? = null

    @Column(name = "assigned_to", nullable = true)
    var assignedTo: String? = null

    @Column(name = "due_at", nullable = true)
    var dueAt: LocalDateTime? = null

    @Column(name = "completed_at", nullable = true)
    var completedAt: LocalDateTime? = null

    @Column(name = "completed_by", nullable = true)
    var completedBy: String? = null
}
