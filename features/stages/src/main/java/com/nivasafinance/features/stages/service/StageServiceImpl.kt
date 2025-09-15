package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.exception.StageExceptionFactory
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.service.TaskService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class StageServiceImpl(
    private val stageRepositoryWrapper: StageRepositoryWrapper,
    private val taskService: TaskService,
    private val messageSource: MessageSource
) : StageService {

    override fun createStage(stageRequest: StageRequest): StageResponse {
        StageExceptionFactory.validateStageForCreation(
            stageRequest.entityType,
            stageRequest.entityId,
            stageRequest.status,
            stageRequest.outcome,
            stageRequest.assignedTo,
            messageSource
        )
        
        val taskResponses = createTasksForStage(UUID.randomUUID(), stageRequest.tasks)
        val taskIds = taskResponses.map { it.id.toString() }
        
        val stage = toStage(stageRequest, taskIds)
        val savedStage = stageRepositoryWrapper.saveWithException(stage)
        
        return toStageResponse(savedStage, taskResponses)
    }

    override fun updateStage(stageId: UUID, stageUpdateRequest: StageUpdateRequest): StageResponse {
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        val updatedStage = stage.copy(
            outcome = stageUpdateRequest.outcome,
            status = stageUpdateRequest.status,
            assignedTo = stageUpdateRequest.assignedTo
        )
        val savedStage = stageRepositoryWrapper.saveWithException(updatedStage)
        return toStageResponse(savedStage)
    }

    override fun getStagesByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<StageResponse> {
        val stages = stageRepositoryWrapper.findAllByEntityTypeAndEntityIdWithException(entityType, entityId)
        return stages.map { stage ->
            val tasks = getTasksByStageId(stage.id!!)
            toStageResponse(stage, tasks)
        }
    }

    override fun getStageById(id: UUID): StageResponse {
        val stage = stageRepositoryWrapper.findByIdWithException(id)
        val tasks = getTasksByStageId(id)
        return toStageResponse(stage, tasks)
    }

    override fun addTasksToStage(stageId: UUID, tasks: List<TaskRequest>): List<TaskResponse> {
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        val taskResponses = createTasksForStage(stageId, tasks)
        val taskIds = taskResponses.map { it.id.toString() }
        val updatedStage = stage.copy(
            tasks = taskIds.toString()
        )
        stageRepositoryWrapper.saveWithException(updatedStage)
        
        return taskResponses
    }

    override fun updateTaskInStage(stageId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): TaskResponse {
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        val taskIds = parseTaskIdsFromStage(stage)
        
        if (!taskIds.contains(taskId)) {
            throw StageExceptionFactory.taskNotFoundInStage(taskId, stageId, messageSource)
        }
        
        val existingTask = taskService.getTask(taskId, messageSource)
        val updatedTask = existingTask.copy(
            taskData = updateTaskRequest.taskData,
            assignedTo = updateTaskRequest.assignedTo,
            status = updateTaskRequest.status,
            outcome = updateTaskRequest.outcome
        )
        
        val savedTask = taskService.updateTask(taskId, updatedTask)
        
        return TaskResponse(
            id = savedTask.id!!,
            taskDefinitionKey = savedTask.taskDefinitionKey,
            taskData = savedTask.taskData,
            assignedTo = savedTask.assignedTo,
            status = savedTask.status,
            outcome = savedTask.outcome,
            dueAt = savedTask.dueAt,
            completedAt = savedTask.completedAt,
            rescheduledAt = savedTask.rescheduledAt,
            createdAt = savedTask.createdAt!!,
            createdBy = savedTask.createdBy,
            updatedAt = savedTask.updatedAt!!,
            updatedBy = savedTask.updatedBy
        )
    }

    override fun deleteTaskFromStage(stageId: UUID, taskId: UUID) {
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        val taskIds = parseTaskIdsFromStage(stage)
        
        if (!taskIds.contains(taskId)) {
            throw StageExceptionFactory.taskNotFoundInStage(taskId, stageId, messageSource)
        }
        
        taskService.deleteTask(taskId, messageSource)
        
        val updatedTask = taskService.getTask(taskId, messageSource)
        val updatedTaskIds = taskIds.filter { it != taskId }
        val updatedStage = stage.copy(
            tasks = updatedTaskIds.toString()
        )
        stageRepositoryWrapper.saveWithException(updatedStage)
    }

    override fun getTasksForStage(stageId: UUID): List<TaskResponse> {
        return getTasksByStageId(stageId)
    }

    private fun toStage(stageRequest: StageRequest, taskIds: List<String> = emptyList()): Stage {
        return Stage(
            stageDefinitionKey = stageRequest.stageDefinitionKey,
            entityType = stageRequest.entityType,
            entityId = stageRequest.entityId,
            outcome = stageRequest.outcome,
            status = stageRequest.status,
            tasks = taskIds.toString(),
            assignedTo = stageRequest.assignedTo
        )
    }

    private fun toStageResponse(stage: Stage, tasks: List<TaskResponse> = emptyList()): StageResponse {
        return StageResponse(
            id = stage.id ?: UUID.randomUUID(),
            entityType = stage.entityType,
            entityId = stage.entityId,
            stageDefinitionKey = stage.stageDefinitionKey,
            outcome = stage.outcome,
            status = stage.status,
            assignedTo = stage.assignedTo,
            tasks = tasks,
            createdAt = stage.createdAt ?: LocalDateTime.now(),
            createdBy = stage.createdBy,
            updatedAt = stage.updatedAt ?: LocalDateTime.now(),
            updatedBy = stage.updatedBy
        )
    }

    private fun createTasksForStage(stageId: UUID, taskRequests: List<TaskRequest>): List<TaskResponse> {
        val taskResponses = taskRequests.map { taskRequest ->
           
            
            val createdTask = taskService.createTask(taskRequest)
            
            TaskResponse(
                id = createdTask.id ?: UUID.randomUUID(),
                taskDefinitionKey = createdTask.taskDefinitionKey,
                taskData = createdTask.taskData,
                assignedTo = createdTask.assignedTo,
                status = createdTask.status,
                outcome = createdTask.outcome,
                dueAt = createdTask.dueAt,
                completedAt = createdTask.completedAt,
                rescheduledAt = createdTask.rescheduledAt,
                createdAt = createdTask.createdAt ?: LocalDateTime.now(),
                createdBy = createdTask.createdBy,
                updatedAt = createdTask.updatedAt ?: LocalDateTime.now(),
                updatedBy = createdTask.updatedBy
            )
        }
        
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        val existingTaskIds = parseTaskIdsFromStage(stage)
        val newTaskIds = taskResponses.map { it.id.toString() }
        val allTaskIds = existingTaskIds + newTaskIds
        
        val updatedStage = stage.copy(
            tasks = allTaskIds.toString()
        )
        stageRepositoryWrapper.saveWithException(updatedStage)
        
        return taskResponses
    }

    private fun getTasksByStageId(stageId: UUID): List<TaskResponse> {
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        val taskIds = parseTaskIdsFromStage(stage)
        
        return taskIds.map { taskId ->
            val task = taskService.getTask(taskId, messageSource)
            TaskResponse(
                id = task.id ?: UUID.randomUUID(),
                taskDefinitionKey = task.taskDefinitionKey,
                taskData = task.taskData,
                assignedTo = task.assignedTo,
                status = task.status,
                outcome = task.outcome,
                dueAt = task.dueAt,
                completedAt = task.completedAt,
                rescheduledAt = task.rescheduledAt,
                createdAt = task.createdAt ?: LocalDateTime.now(),
                createdBy = task.createdBy,
                updatedAt = task.updatedAt ?: LocalDateTime.now(),
                updatedBy = task.updatedBy
            )
        }
    }

    private fun parseTaskIdsFromStage(stage: Stage): List<UUID> {
        val taskIdsString = stage.tasks ?: "[]"
        return if (taskIdsString.isNotEmpty() && taskIdsString != "[]") {
            taskIdsString.replace("[", "").replace("]", "").replace("\"", "")
                .split(",").map { UUID.fromString(it.trim()) }
        } else {
            emptyList()
        }
    }
}