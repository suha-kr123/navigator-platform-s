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

    fun existsByTaskKeyWithException(taskKey: String): Boolean {
        return try {
            taskRepository.existsByTaskKey(taskKey)
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityFailed(messageSource)
        }
    } 

    fun findAllWithException(pageable: Pageable): Page<Task> {
        return try {
            taskRepository.findAll(pageable)
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityTypeFailed(messageSource)
        }
    }

    fun countWithException(): Long {
        return try {   
            taskRepository.count()
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityTypeFailed(messageSource)
        }
    }

    fun saveWithException(task: Task): Task {
        return try {
            taskRepository.save(task)
        } catch (e: Exception) {
            throw TaskExceptionFactory.createFailed(messageSource)
        }
    }

    fun deleteByIdWithException(taskId: UUID) {
        try {
            taskRepository.deleteById(taskId)
        } catch (e: Exception) {
            throw TaskExceptionFactory.deleteFailed(messageSource)
        }
    }

    fun findByIdWithException(taskId: UUID): Task {
        return try {
            taskRepository.findById(taskId).orElseThrow {
                TaskExceptionFactory.taskNotFound(taskId, messageSource)
            }
        } catch (e: Exception) {
            throw TaskExceptionFactory.taskNotFound(taskId, messageSource)
        }
    }


}