package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.exception.StageExceptionFactory
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.stagetasks.dto.StageTaskRequest
import com.nivasafinance.features.stagetasks.service.StageTaskService
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
    private val stageTaskService: StageTaskService,
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
        
        val stage = toStage(stageRequest)
        val savedStage = stageRepositoryWrapper.saveWithException(stage)
        
        val taskResponses = createTasksForStage(savedStage.id!!, stageRequest.tasks)
        
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
        stageRepositoryWrapper.findByIdWithException(stageId)
        return createTasksForStage(stageId, tasks)
    }

    override fun updateTaskInStage(stageId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): TaskResponse {
        stageRepositoryWrapper.findByIdWithException(stageId)
        
        val stageTasks = stageTaskService.getTasksForStage(stageId)
        val taskExists = stageTasks.any { it.taskId == taskId }
        
        if (!taskExists) {
            throw StageExceptionFactory.taskNotFoundInStage(taskId, stageId, messageSource)
        }
        
        val savedTask = taskService.updateTask(taskId, updateTaskRequest)
        
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
        stageRepositoryWrapper.findByIdWithException(stageId)
        
        val stageTasks = stageTaskService.getTasksForStage(stageId)
        val taskExists = stageTasks.any { it.taskId == taskId }
        
        if (!taskExists) {
            throw StageExceptionFactory.taskNotFoundInStage(taskId, stageId, messageSource)
        }
        
        stageTaskService.deleteStageTask(stageId, taskId)
        taskService.deleteTask(taskId)
    }

    override fun getTasksForStage(stageId: UUID): List<TaskResponse> {
        val stageTasks = stageTaskService.getTasksForStage(stageId)
        return stageTasks.map { stageTask ->
            taskService.getTask(stageTask.taskId)
        }
    }

    private fun toStage(stageRequest: StageRequest): Stage {
        return Stage(
            stageDefinitionKey = stageRequest.stageDefinitionKey,
            entityType = stageRequest.entityType,
            entityId = stageRequest.entityId,
            outcome = stageRequest.outcome,
            status = stageRequest.status,
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
            
            stageTaskService.createStageTask(
                StageTaskRequest(
                    stageId = stageId,
                    taskId = createdTask.id!!,
                    extData = null
                )
            )
            
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
                createdAt = createdTask.createdAt,
                createdBy = createdTask.createdBy,
                updatedAt = createdTask.updatedAt,
                updatedBy = createdTask.updatedBy
            )
        }
        
        return taskResponses
    }

    private fun getTasksByStageId(stageId: UUID): List<TaskResponse> {
        val stageTasks = stageTaskService.getTasksForStage(stageId)
        return stageTasks.map { stageTask ->
            taskService.getTask(stageTask.taskId)
        }
    }

}