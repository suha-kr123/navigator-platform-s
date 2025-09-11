package com.nivasafinance.features.tasks.dto

import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

sealed class TaskCommand {
    abstract val commandId: UUID
    abstract val timestamp: LocalDateTime
}

data class CreateTaskCommand(
    override val commandId: UUID,
    override val timestamp: LocalDateTime,
    val taskKey: String,
    val leadId: UUID,
    val stage: String,
    val taskData: Map<String, Any>? = null,
    val assignedTo: String? = null,
    val status: TaskStatus,
    val dueAt: LocalDateTime? = null
) : TaskCommand()

data class UpdateTaskCommand(
    override val commandId: UUID,
    override val timestamp: LocalDateTime,
    val taskId: UUID,
    val taskData: Map<String, Any>? = null,
    val assignedTo: String? = null,
    val status: TaskStatus? = null,
    val dueAt: LocalDateTime? = null
) : TaskCommand()

data class CompleteTaskCommand(
    override val commandId: UUID,
    override val timestamp: LocalDateTime,
    val taskId: UUID
) : TaskCommand()

data class RescheduleTaskCommand(
    override val commandId: UUID,
    override val timestamp: LocalDateTime,
    val taskId: UUID,
    val newDueAt: LocalDateTime
) : TaskCommand()

data class DeleteTaskCommand(
    override val commandId: UUID,
    override val timestamp: LocalDateTime,
    val taskId: UUID
) : TaskCommand()
