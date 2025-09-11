package com.nivasafinance.features.tasks.service.impl

import com.nivasafinance.features.tasks.entity.LeadTask
import com.nivasafinance.features.tasks.event.TaskCompletedEvent
import com.nivasafinance.features.tasks.event.TaskCreatedEvent
import com.nivasafinance.features.tasks.event.TaskDeletedEvent
import com.nivasafinance.features.tasks.event.TaskRescheduledEvent
import com.nivasafinance.features.tasks.event.TaskUpdatedEvent
import com.nivasafinance.features.tasks.repository.LeadTaskRepository
import com.nivasafinance.features.tasks.service.TaskEventProcessor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskEventProcessorImpl(
    private val leadTaskRepository: LeadTaskRepository
) : TaskEventProcessor {

    @Transactional
    override fun processTaskCreated(event: TaskCreatedEvent) {
        val leadTask = LeadTask(
            taskKey = event.taskKey,
            leadId = event.leadId,
            stage = event.stage,
            taskData = event.taskData,
            assignedTo = event.assignedTo,
            status = event.status,
            dueAt = event.dueAt
        )
        leadTaskRepository.save(leadTask)
    }

    @Transactional
    override fun processTaskUpdated(event: TaskUpdatedEvent) {
        val leadTask = leadTaskRepository.findByLeadIdAndTaskKey(event.leadId, event.taskKey)
            ?: return

        event.taskData?.let { leadTask.taskData = it }
        event.assignedTo?.let { leadTask.assignedTo = it }
        event.status?.let { leadTask.status = it }
        event.dueAt?.let { leadTask.dueAt = it }

        leadTaskRepository.save(leadTask)
    }

    @Transactional
    override fun processTaskCompleted(event: TaskCompletedEvent) {
        val leadTask = leadTaskRepository.findByLeadIdAndTaskKey(event.leadId, event.taskKey)
            ?: return

        leadTask.status = com.nivasafinance.features.tasks.enum.TaskStatus.COMPLETED
        leadTask.completedAt = event.completedAt

        leadTaskRepository.save(leadTask)
    }

    @Transactional
    override fun processTaskRescheduled(event: TaskRescheduledEvent) {
        val leadTask = leadTaskRepository.findByLeadIdAndTaskKey(event.leadId, event.taskKey)
            ?: return

        leadTask.dueAt = event.newDueAt
        leadTask.rescheduledAt = event.rescheduledAt

        leadTaskRepository.save(leadTask)
    }

    @Transactional
    override fun processTaskDeleted(event: TaskDeletedEvent) {
        val leadTask = leadTaskRepository.findByLeadIdAndTaskKey(event.leadId, event.taskKey)
            ?: return

        leadTaskRepository.delete(leadTask)
    }
}
