package com.nivasafinance.features.taskdefinitions.repository

import com.nivasafinance.features.taskdefinitions.entity.TaskDefinition
import com.nivasafinance.features.taskdefinitions.exception.TaskDefinitionExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class TaskDefinitionRepositoryWrapper(
    private val taskDefinitionRepository: TaskDefinitionRepository,
    private val messageSource: MessageSource
) {

    fun findByKeyWithException(key: String): TaskDefinition {
        return taskDefinitionRepository.findByKey(key) ?: throw TaskDefinitionExceptionFactory.retrieveFailed(
            messageSource
        )
    }

    fun findAllWithException(): List<TaskDefinition> {
        return try {
            taskDefinitionRepository.findAll()
        } catch (e: Exception) {
            throw TaskDefinitionExceptionFactory.retrieveFailed(messageSource)
        }
    }
}
