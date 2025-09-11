package com.nivasafinance.features.taskdefinitions.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionCreateRequest
import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionUpdateRequest
import com.nivasafinance.features.taskdefinitions.entity.TaskDefinition
import com.nivasafinance.features.taskdefinitions.exception.TaskDefinitionConflictException
import com.nivasafinance.features.taskdefinitions.exception.TaskDefinitionNotFoundException
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepository
import com.nivasafinance.features.taskdefinitions.service.TaskDefinitionService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "taskDefinitionCacheManager")
class TaskDefinitionServiceImpl(
    private val taskDefinitionRepository: TaskDefinitionRepository
) : TaskDefinitionService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "taskDefinition"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getTaskDefinition(id: UUID): TaskDefinitionResponse {
        val taskDefinition = taskDefinitionRepository.findById(id)
            .orElseThrow { TaskDefinitionNotFoundException(id, messageSource) }
        return buildTaskDefinitionResponseFromEntity(taskDefinition)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'key:' + #key")
    override fun getTaskDefinitionByKey(key: String): TaskDefinitionResponse {
        val taskDefinition = taskDefinitionRepository.findByKey(key)
            ?: throw TaskDefinitionNotFoundException(UUID.randomUUID(), messageSource)
        return buildTaskDefinitionResponseFromEntity(taskDefinition)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'all'")
    override fun getAllTaskDefinitions(): List<TaskDefinitionResponse> {
        return taskDefinitionRepository.findAll().map { buildTaskDefinitionResponseFromEntity(it) }
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'actionsGroup:' + #actionsGroup")
    override fun getTaskDefinitionsByActionsGroup(actionsGroup: String): List<TaskDefinitionResponse> {
        return taskDefinitionRepository.findByActionsGroup(actionsGroup)
            .map { buildTaskDefinitionResponseFromEntity(it) }
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'identifier:' + #identifier")
    override fun getTaskDefinitionsByIdentifier(identifier: String): List<TaskDefinitionResponse> {
        return taskDefinitionRepository.findByIdentifier(identifier)
            .map { buildTaskDefinitionResponseFromEntity(it) }
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#result.id")],
        evict = [CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)]
    )
    override fun createTaskDefinition(request: TaskDefinitionCreateRequest): TaskDefinitionResponse {
        if (taskDefinitionRepository.existsByKey(request.key)) {
            throw TaskDefinitionConflictException(request.key, messageSource)
        }

        val taskDefinition = TaskDefinition(
            name = request.name,
            identifier = request.identifier,
            key = request.key,
            description = request.description,
            actionsGroup = request.actionsGroup,
            conditionOnAction = request.conditionOnAction,
            tatHours = request.tatHours,
            assignmentStrategy = request.assignmentStrategy,
            priority = request.priority,
            possibleStatuses = request.possibleStatuses
        )

        val savedTaskDefinition = taskDefinitionRepository.save(taskDefinition)
        return buildTaskDefinitionResponseFromEntity(savedTaskDefinition)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#id")],
        evict = [CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)]
    )
    override fun updateTaskDefinition(id: UUID, request: TaskDefinitionUpdateRequest): TaskDefinitionResponse {
        val existingTaskDefinition = taskDefinitionRepository.findById(id).orElseThrow {
            TaskDefinitionNotFoundException(id, messageSource)
        }

        val updatedTaskDefinition = TaskDefinition(
            id = existingTaskDefinition.id,
            name = request.name ?: existingTaskDefinition.name,
            identifier = request.identifier ?: existingTaskDefinition.identifier,
            key = request.key ?: existingTaskDefinition.key,
            description = request.description ?: existingTaskDefinition.description,
            actionsGroup = request.actionsGroup ?: existingTaskDefinition.actionsGroup,
            conditionOnAction = request.conditionOnAction ?: existingTaskDefinition.conditionOnAction,
            tatHours = request.tatHours ?: existingTaskDefinition.tatHours,
            assignmentStrategy = request.assignmentStrategy ?: existingTaskDefinition.assignmentStrategy,
            priority = request.priority ?: existingTaskDefinition.priority,
            possibleStatuses = request.possibleStatuses ?: existingTaskDefinition.possibleStatuses
        )

        val savedTaskDefinition = taskDefinitionRepository.save(updatedTaskDefinition)
        return buildTaskDefinitionResponseFromEntity(savedTaskDefinition)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#id"),
            CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)
        ]
    )
    override fun deleteTaskDefinition(id: UUID) {
        val taskDefinition = taskDefinitionRepository.findById(id).orElseThrow {
            TaskDefinitionNotFoundException(id, messageSource)
        }
        taskDefinitionRepository.deleteById(taskDefinition.id ?: error("Task definition ID is null"))
    }

    override fun existsByKey(key: String): Boolean {
        return taskDefinitionRepository.existsByKey(key)
    }

    private fun buildTaskDefinitionResponseFromEntity(taskDefinition: TaskDefinition): TaskDefinitionResponse {
        return TaskDefinitionResponse(
            id = taskDefinition.id ?: error("Task definition ID is null"),
            name = taskDefinition.name,
            identifier = taskDefinition.identifier,
            key = taskDefinition.key,
            description = taskDefinition.description,
            actionsGroup = taskDefinition.actionsGroup,
            conditionOnAction = taskDefinition.conditionOnAction,
            tatHours = taskDefinition.tatHours,
            assignmentStrategy = taskDefinition.assignmentStrategy,
            priority = taskDefinition.priority,
            possibleStatuses = taskDefinition.possibleStatuses
        )
    }
}
