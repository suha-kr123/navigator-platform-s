package com.nivasafinance.features.taskdefinitions.service

import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
import com.nivasafinance.features.taskdefinitions.exception.TaskDefinitionExceptionFactory
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class TaskDefinitionServiceImpl(
    private val taskDefinitionRepositoryWrapper: TaskDefinitionRepositoryWrapper,
    private val messageSource: MessageSource
) : TaskDefinitionService {

    override fun getTaskOutcomesByKey(key: String): TaskOutcomesResponse {
        val taskDefinition = taskDefinitionRepositoryWrapper.findByKeyWithException(key)
            ?: throw TaskDefinitionExceptionFactory.notFound(key, messageSource)
        
        val outcomes = taskDefinition.possibleOutcomes ?: emptyList()
        
        return TaskOutcomesResponse(
            taskDefinitionKey = taskDefinition.key,
            taskDefinitionName = taskDefinition.name,
            outcomes = outcomes
        )
    }
}
