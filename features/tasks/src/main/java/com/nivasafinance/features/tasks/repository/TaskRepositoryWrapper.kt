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

    fun findByIdWithException(id: UUID): Task {
        return try {
            taskRepository.findById(id).orElseThrow {
                TaskExceptionFactory.taskNotFound(id, messageSource)
            }
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllWithException(pageable: Pageable): Page<Task> {
        return try {
            taskRepository.findAll(pageable)
        } catch (e: Exception) {
            throw TaskExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            if (!taskRepository.existsById(id)) {
                throw TaskExceptionFactory.taskNotFound(id, messageSource)
            }
            taskRepository.deleteById(id)
        } catch (e: Exception) {
            throw TaskExceptionFactory.deleteFailed(messageSource)
        }
    }
}
