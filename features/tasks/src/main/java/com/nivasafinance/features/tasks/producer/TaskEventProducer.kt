package com.nivasafinance.features.tasks.producer

import com.nivasafinance.features.tasks.event.TaskEvent
import event.EventService
import event.EventTopics
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Component
class TaskEventProducer(
    private val eventService: EventService
) {

    fun publishTaskEvent(event: TaskEvent): CompletableFuture<Unit> {
        val topic = when (event.eventType) {
            "TaskCreated" -> EventTopics.TASK_CREATED
            "TaskUpdated" -> EventTopics.TASK_UPDATED
            "TaskCompleted" -> EventTopics.TASK_COMPLETED
            "TaskRescheduled" -> EventTopics.TASK_RESCHEDULED
            "TaskDeleted" -> EventTopics.TASK_DELETED
            else -> throw IllegalArgumentException("Unknown event type: ${event.eventType}")
        }
        return eventService.publishEvent(event, topic)
    }

    fun publishTaskCreated(
        leadId: UUID,
        taskKey: String,
        stage: String,
        taskData: Map<String, Any>? = null,
        assignedTo: String? = null,
        status: com.nivasafinance.features.tasks.enum.TaskStatus,
        dueAt: java.time.LocalDateTime? = null
    ): CompletableFuture<Unit> {
        val event = com.nivasafinance.features.tasks.event.TaskCreatedEvent(
            eventId = UUID.randomUUID(),
            timestamp = java.time.LocalDateTime.now(),
            leadId = leadId,
            taskKey = taskKey,
            stage = stage,
            taskData = taskData,
            assignedTo = assignedTo,
            status = status,
            dueAt = dueAt
        )
        return publishTaskEvent(event)
    }

    fun publishTaskCompleted(
        leadId: UUID,
        taskKey: String
    ): CompletableFuture<Unit> {
        val event = com.nivasafinance.features.tasks.event.TaskCompletedEvent(
            eventId = UUID.randomUUID(),
            timestamp = java.time.LocalDateTime.now(),
            leadId = leadId,
            taskKey = taskKey,
            completedAt = java.time.LocalDateTime.now()
        )
        return publishTaskEvent(event)
    }

    fun publishTaskRescheduled(
        leadId: UUID,
        taskKey: String,
        newDueAt: java.time.LocalDateTime
    ): CompletableFuture<Unit> {
        val event = com.nivasafinance.features.tasks.event.TaskRescheduledEvent(
            eventId = UUID.randomUUID(),
            timestamp = java.time.LocalDateTime.now(),
            leadId = leadId,
            taskKey = taskKey,
            newDueAt = newDueAt,
            rescheduledAt = java.time.LocalDateTime.now()
        )
        return publishTaskEvent(event)
    }
}
