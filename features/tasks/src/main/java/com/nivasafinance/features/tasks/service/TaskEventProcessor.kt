package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.event.TaskCompletedEvent
import com.nivasafinance.features.tasks.event.TaskCreatedEvent
import com.nivasafinance.features.tasks.event.TaskDeletedEvent
import com.nivasafinance.features.tasks.event.TaskRescheduledEvent
import com.nivasafinance.features.tasks.event.TaskUpdatedEvent

interface TaskEventProcessor {
    fun processTaskCreated(event: TaskCreatedEvent)
    fun processTaskUpdated(event: TaskUpdatedEvent)
    fun processTaskCompleted(event: TaskCompletedEvent)
    fun processTaskRescheduled(event: TaskRescheduledEvent)
    fun processTaskDeleted(event: TaskDeletedEvent)
}
