package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.*
import com.nivasafinance.features.lead.repository.LeadRepository
import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.repository.TaskRepository
import com.nivasafinance.features.tasks.taskstagemapping.service.TaskStageMappingService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class LeadTaskServiceImpl(
    private val leadRepository: LeadRepository,
    private val taskRepository: TaskRepository,
    private val taskStageMappingService: TaskStageMappingService
) : LeadTaskService {

    override fun createTaskForLead(leadId: UUID, stageKey: String, request: CreateTaskRequest): TaskResponse {
        leadRepository.findById(leadId)
            .orElseThrow { IllegalArgumentException("Lead not found with id: $leadId") }

        val task = Task(
            taskKey = request.taskKey,
            entityId = leadId,
            entityType = "LEAD",
            taskData = request.taskData,
            assignedTo = request.assignedTo,
            status = request.status,
            outcome = request.outcome,
            dueAt = request.dueAt
        )

        val savedTask = taskRepository.save(task)
        taskStageMappingService.createTaskStageMapping(savedTask.id!!, leadId, "LEAD", stageKey)

        return TaskResponse(
            id = savedTask.id!!,
            taskKey = savedTask.taskKey,
            entityId = savedTask.entityId,
            entityType = savedTask.entityType,
            assignedTo = savedTask.assignedTo,
            status = savedTask.status,
            outcome = savedTask.outcome,
            taskData = savedTask.taskData,
            dueAt = savedTask.dueAt,
            completedAt = savedTask.completedAt,
            rescheduledAt = savedTask.rescheduledAt,
            createdAt = savedTask.createdAt,
            updatedAt = savedTask.updatedAt
        )
    }

    override fun updateTaskForLead(leadId: UUID, taskId: UUID, request: UpdateTaskRequest): TaskResponse {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found with id: $taskId") }

        if (task.entityId != leadId || task.entityType != "LEAD") {
            throw IllegalArgumentException("Task does not belong to the specified lead")
        }

        val updatedTask = task.copy(
            status = request.status ?: task.status,
            outcome = request.outcome ?: task.outcome,
            assignedTo = request.assignedTo ?: task.assignedTo,
            taskData = request.taskData ?: task.taskData,
            dueAt = request.dueAt ?: task.dueAt,
            completedAt = request.completedAt ?: task.completedAt,
            rescheduledAt = request.rescheduledAt ?: task.rescheduledAt
        )

        val savedTask = taskRepository.save(updatedTask)

        return TaskResponse(
            id = savedTask.id!!,
            taskKey = savedTask.taskKey,
            entityId = savedTask.entityId,
            entityType = savedTask.entityType,
            assignedTo = savedTask.assignedTo,
            status = savedTask.status,
            outcome = savedTask.outcome,
            taskData = savedTask.taskData,
            dueAt = savedTask.dueAt,
            completedAt = savedTask.completedAt,
            rescheduledAt = savedTask.rescheduledAt,
            createdAt = savedTask.createdAt,
            updatedAt = savedTask.updatedAt
        )
    }

    override fun deleteTaskForLead(leadId: UUID, taskId: UUID) {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found with id: $taskId") }

        if (task.entityId != leadId || task.entityType != "LEAD") {
            throw IllegalArgumentException("Task does not belong to the specified lead")
        }

        taskStageMappingService.deleteTaskStageMapping(taskId)
        taskRepository.deleteById(taskId)
    }

    override fun getTasksForLead(leadId: UUID): List<TaskResponse> {
        val tasks = taskRepository.findByEntityIdAndEntityType(leadId, "LEAD")
        return tasks.map { task ->
            TaskResponse(
                id = task.id!!,
                taskKey = task.taskKey,
                entityId = task.entityId,
                entityType = task.entityType,
                assignedTo = task.assignedTo,
                status = task.status,
                outcome = task.outcome,
                taskData = task.taskData,
                dueAt = task.dueAt,
                completedAt = task.completedAt,
                rescheduledAt = task.rescheduledAt,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
        }
    }

    override fun getTasksForLeadAndStage(leadId: UUID, stageKey: String): List<TaskResponse> {
        val taskIds = taskStageMappingService.getTasksForEntityAndStage(leadId, "LEAD", stageKey)
        val tasks = taskRepository.findAllById(taskIds)
        return tasks.map { task ->
            TaskResponse(
                id = task.id!!,
                taskKey = task.taskKey,
                entityId = task.entityId,
                entityType = task.entityType,
                assignedTo = task.assignedTo,
                status = task.status,
                outcome = task.outcome,
                taskData = task.taskData,
                dueAt = task.dueAt,
                completedAt = task.completedAt,
                rescheduledAt = task.rescheduledAt,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
        }
    }

    override fun getLeadsForStage(stageKey: String): List<LeadResponse> {
        val leads = leadRepository.findByStage(stageKey)
        return leads.map { lead ->
            LeadResponse(
                id = lead.id!!,
                requestedAmount = lead.requestedAmount,
                purpose = lead.purpose,
                productCode = lead.productCode,
                status = lead.status,
                sourcingChannel = lead.sourcingChannel,
                preliminaryInformation = lead.preliminaryInformation,
                leadContacts = lead.leadContacts,
                extData = lead.extData,
                createdAt = lead.createdAt,
                updatedAt = lead.updatedAt
            )
        }
    }

    override fun getLeadsForStageAndPipeline(stageKey: String, pipelineKey: String): List<LeadResponse> {
        val leads = leadRepository.findByStageAndPipeline(stageKey, pipelineKey)
        return leads.map { lead ->
            LeadResponse(
                id = lead.id!!,
                requestedAmount = lead.requestedAmount,
                purpose = lead.purpose,
                productCode = lead.productCode,
                status = lead.status,
                sourcingChannel = lead.sourcingChannel,
                preliminaryInformation = lead.preliminaryInformation,
                leadContacts = lead.leadContacts,
                extData = lead.extData,
                createdAt = lead.createdAt,
                updatedAt = lead.updatedAt
            )
        }
    }
}
