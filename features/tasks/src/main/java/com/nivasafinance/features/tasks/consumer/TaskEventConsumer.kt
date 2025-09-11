package com.nivasafinance.features.tasks.consumer

import com.nivasafinance.features.tasks.event.TaskCompletedEvent
import com.nivasafinance.features.tasks.event.TaskCreatedEvent
import com.nivasafinance.features.tasks.event.TaskDeletedEvent
import com.nivasafinance.features.tasks.event.TaskEvent
import com.nivasafinance.features.tasks.event.TaskRescheduledEvent
import com.nivasafinance.features.tasks.event.TaskUpdatedEvent
import com.nivasafinance.features.tasks.service.TaskEventProcessor
import event.EventTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class TaskEventConsumer(
    private val taskEventProcessor: TaskEventProcessor
) {
    companion object {
        private const val GROUP_ID = "task-processor-group"
    }

    @KafkaListener(
        topics = [EventTopics.TASK_CREATED, EventTopics.TASK_UPDATED, EventTopics.TASK_COMPLETED, EventTopics.TASK_RESCHEDULED, EventTopics.TASK_DELETED],
        groupId = GROUP_ID,
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleTaskEvent(
        @Payload event: TaskEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            when (event) {
                is TaskCreatedEvent -> taskEventProcessor.processTaskCreated(event)
                is TaskUpdatedEvent -> taskEventProcessor.processTaskUpdated(event)
                is TaskCompletedEvent -> taskEventProcessor.processTaskCompleted(event)
                is TaskRescheduledEvent -> taskEventProcessor.processTaskRescheduled(event)
                is TaskDeletedEvent -> taskEventProcessor.processTaskDeleted(event)
            }
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            // Log error and handle retry logic
            // For now, we'll acknowledge to prevent infinite retries
            // In production, implement proper error handling and DLQ
            acknowledgment.acknowledge()
        }
    }
}
