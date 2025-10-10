package com.nivasafinance.features.taskdefinitions.service

import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepositoryWrapper
import com.nivasafinance.features.tasks.enum.TaskStatus
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class TaskDefinitionServiceImpl(
    private val taskDefinitionRepositoryWrapper: TaskDefinitionRepositoryWrapper,
    private val messageSource: MessageSource
) : TaskDefinitionService {

    override fun getTaskOutcomesByKey(key: String, status: TaskStatus?): TaskOutcomesResponse {
        val taskDefinition = taskDefinitionRepositoryWrapper.findByKeyWithException(key)

        val outcomeConfig = taskDefinition.outcomeConfiguration

        // If status is specified, return outcomes for that status only
        val outcomes = if (status != null) {
            getValidOutcomesFromMap(outcomeConfig, status)
        } else {
            // If no status specified, return all possible outcomes
            getAllPossibleOutcomesFromMap(outcomeConfig)
        }

        val statusOutcomeMapping = convertToStatusOutcomeMapping(outcomeConfig)

        return TaskOutcomesResponse(
            taskDefinitionKey = taskDefinition.key,
            taskDefinitionName = taskDefinition.name,
            outcomes = outcomes,
            statusOutcomeMapping = statusOutcomeMapping
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun getValidOutcomesFromMap(
        outcomeConfig: Map<String, Any>?,
        status: TaskStatus
    ): List<com.nivasafinance.features.tasks.dto.TaskOutcome> {
        if (outcomeConfig == null) return emptyList()

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any> ?: return emptyList()
        val statusOutcomes = statusOutcomeMapping[status.value] as? List<Map<String, Any>> ?: return emptyList()

        return statusOutcomes.map { outcomeMap ->
            com.nivasafinance.features.tasks.dto.TaskOutcome(
                code = outcomeMap["code"] as? String ?: "",
                name = outcomeMap["name"] as? String ?: "",
                description = outcomeMap["description"] as? String ?: ""
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getAllPossibleOutcomesFromMap(
        outcomeConfig: Map<String, Any>?
    ): List<com.nivasafinance.features.tasks.dto.TaskOutcome> {
        if (outcomeConfig == null) return emptyList()

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any> ?: return emptyList()
        val allOutcomes = mutableSetOf<com.nivasafinance.features.tasks.dto.TaskOutcome>()

        statusOutcomeMapping.values.forEach { statusOutcomes ->
            val outcomes = statusOutcomes as? List<Map<String, Any>> ?: return@forEach
            outcomes.forEach { outcomeMap ->
                allOutcomes.add(
                    com.nivasafinance.features.tasks.dto.TaskOutcome(
                        code = outcomeMap["code"] as? String ?: "",
                        name = outcomeMap["name"] as? String ?: "",
                        description = outcomeMap["description"] as? String ?: ""
                    )
                )
            }
        }

        return allOutcomes.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertToStatusOutcomeMapping(
        outcomeConfig: Map<String, Any>?
    ): Map<String, List<com.nivasafinance.features.tasks.dto.TaskOutcome>>? {
        if (outcomeConfig == null) return null

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any> ?: return null
        val result = mutableMapOf<String, List<com.nivasafinance.features.tasks.dto.TaskOutcome>>()

        statusOutcomeMapping.forEach { (status, outcomes) ->
            val outcomeList = outcomes as? List<Map<String, Any>> ?: return@forEach
            result[status] = outcomeList.map { outcomeMap ->
                com.nivasafinance.features.tasks.dto.TaskOutcome(
                    code = outcomeMap["code"] as? String ?: "",
                    name = outcomeMap["name"] as? String ?: "",
                    description = outcomeMap["description"] as? String ?: ""
                )
            }
        }

        return result
    }

    override fun getAllTaskDefinitions(): List<TaskDefinitionResponse> {
        val taskDefinitions = taskDefinitionRepositoryWrapper.findAllWithException()

        return taskDefinitions.map { taskDefinition ->
            TaskDefinitionResponse(
                id = taskDefinition.id!!,
                name = taskDefinition.name,
                key = taskDefinition.key,
                type = taskDefinition.type.value,
                description = taskDefinition.description
            )
        }
    }
}
