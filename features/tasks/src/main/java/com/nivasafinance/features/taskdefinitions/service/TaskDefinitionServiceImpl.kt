package com.nivasafinance.features.taskdefinitions.service

import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionListResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
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

        val outcomes = taskDefinition.possibleOutcomes ?: emptyList()

        return TaskOutcomesResponse(
            taskDefinitionKey = taskDefinition.key,
            taskDefinitionName = taskDefinition.name,
            outcomes = outcomes
        )
    }

    override fun getAllTaskDefinitions(): TaskDefinitionListResponse {
        val taskDefinitions = taskDefinitionRepositoryWrapper.findAllWithException()

        val taskDefinitionResponses = taskDefinitions.map { taskDefinition ->
            TaskDefinitionResponse(
                id = taskDefinition.id!!,
                name = taskDefinition.name,
                key = taskDefinition.key,
                type = taskDefinition.type.value,
                description = taskDefinition.description,
                possibleOutcomes = taskDefinition.possibleOutcomes
            )
        }

        return TaskDefinitionListResponse(taskDefinitions = taskDefinitionResponses)
    }
}
