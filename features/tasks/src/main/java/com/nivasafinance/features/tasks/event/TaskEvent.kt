package com.nivasafinance.features.tasks.event

import com.nivasafinance.features.tasks.enum.TaskStatus
import event.BaseDomainEvent
import java.time.LocalDateTime
import java.util.UUID

sealed class TaskEvent : BaseDomainEvent {
    abstract val leadId: UUID
    abstract val taskKey: String

    override val aggregateType: String = "Task"
    override val source: String = "tasks-service"
    override val version: Int = 1
}

data class TaskCreatedEvent(
    override val eventId: UUID,
    override val timestamp: LocalDateTime,
    override val leadId: UUID,
    override val taskKey: String,
    val stage: String,
    val taskData: Map<String, Any>? = null,
    val assignedTo: String? = null,
    val status: TaskStatus,
    val dueAt: LocalDateTime? = null
) : TaskEvent() {
    override val eventType: String = "TaskCreated"
    override val aggregateId: String = "$leadId-$taskKey"
}

data class TaskUpdatedEvent(
    override val eventId: UUID,
    override val timestamp: LocalDateTime,
    override val leadId: UUID,
    override val taskKey: String,
    val taskData: Map<String, Any>? = null,
    val assignedTo: String? = null,
    val status: TaskStatus? = null,
    val dueAt: LocalDateTime? = null
) : TaskEvent() {
    override val eventType: String = "TaskUpdated"
    override val aggregateId: String = "$leadId-$taskKey"
}

data class TaskCompletedEvent(
    override val eventId: UUID,
    override val timestamp: LocalDateTime,
    override val leadId: UUID,
    override val taskKey: String,
    val completedAt: LocalDateTime
) : TaskEvent() {
    override val eventType: String = "TaskCompleted"
    override val aggregateId: String = "$leadId-$taskKey"
}

data class TaskRescheduledEvent(
    override val eventId: UUID,
    override val timestamp: LocalDateTime,
    override val leadId: UUID,
    override val taskKey: String,
    val newDueAt: LocalDateTime,
    val rescheduledAt: LocalDateTime
) : TaskEvent() {
    override val eventType: String = "TaskRescheduled"
    override val aggregateId: String = "$leadId-$taskKey"
}

data class TaskDeletedEvent(
    override val eventId: UUID,
    override val timestamp: LocalDateTime,
    override val leadId: UUID,
    override val taskKey: String
) : TaskEvent() {
    override val eventType: String = "TaskDeleted"
    override val aggregateId: String = "$leadId-$taskKey"
}
