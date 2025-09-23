package com.nivasafinance.features.tasks.repository

import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.exception.TaskExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaskRepositoryWrapper(
    private val taskRepository: TaskRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(task: Task): Task {
        return try {
            taskRepository.save(task)
        } catch (e: Exception) {
            throw TaskExceptionFactory.createFailed(messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityIdWithException(entityType: String, entityId: UUID): List<Task> {
        return try {
            taskRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun existsByEntityTypeAndEntityIdAndTaskDefinitionKeyWithException(entityType: String, entityId: UUID, taskDefinitionKey: String): Boolean {
        return try {
            taskRepository.existsByEntityTypeAndEntityIdAndTaskDefinitionKey(entityType, entityId, taskDefinitionKey)
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityIdInWithException(entityType: String, entityIds: List<UUID>, pageable: Pageable): Page<Task> {
        return try {
            taskRepository.findAllByEntityTypeAndEntityIdIn(entityType, entityIds, pageable)
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}