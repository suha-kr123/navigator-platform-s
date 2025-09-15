package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.exception.TaskExceptionFactory
import com.nivasafinance.features.tasks.repository.TaskRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TaskServiceImpl(
    private val taskRepositoryWrapper: TaskRepositoryWrapper,
    private val messageSource: MessageSource
) : TaskService {

    override fun createTask(task: Task): Task {
        TaskExceptionFactory.validateTaskForCreation(
            task.taskDefinitionKey,
            task.taskDefinitionKey,
            task.status,
            task.outcome,
            task.dueAt,
            task.assignedTo,
            messageSource
        )
        return try {
            taskRepositoryWrapper.save(task)
        } catch (e: Exception) {
            e.printStackTrace()
            throw TaskExceptionFactory.createFailed(messageSource)
        }
    }

    override fun updateTask(task: Task): Task {
        TaskExceptionFactory.validateTaskForUpdate(
            task.id,
            task.taskDefinitionKey,
            task.taskDefinitionKey,
            task.status,
            task.outcome,
            task.dueAt,
            task.assignedTo,
            messageSource
        )
        return taskRepositoryWrapper.save(task)
    }

    override fun deleteTask(taskId: UUID) {
        getTaskById(taskId)
        taskRepositoryWrapper.deleteById(taskId)
    }

    override fun getTask(taskId: UUID): Task {
        return getTaskById(taskId)
    }

    private fun getTaskById(taskId: UUID): Task {
        return taskRepositoryWrapper.findById(taskId)
    }
}