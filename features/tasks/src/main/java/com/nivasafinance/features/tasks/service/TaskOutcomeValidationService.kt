package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.taskdefinitions.entity.TaskDefinition
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepository
import com.nivasafinance.features.tasks.enum.TaskStatus
import com.nivasafinance.features.tasks.exception.TaskValidationException
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class TaskOutcomeValidationService(
    private val taskDefinitionRepository: TaskDefinitionRepository,
    private val messageSource: MessageSource
) {

    /**
     * Validates if an outcome is valid for a given task definition and status
     */
    fun validateOutcome(taskDefinitionKey: String, status: TaskStatus, outcome: String?): Boolean {
        if (outcome == null) return true // null outcomes are allowed

        val taskDefinition = taskDefinitionRepository.findByKey(taskDefinitionKey)
            ?: throw TaskValidationException("error.task.definition.not.found", arrayOf(taskDefinitionKey), messageSource)

        return validateOutcome(taskDefinition, status, outcome)
    }

    /**
     * Validates if an outcome is valid for a given task definition and status
     */
    fun validateOutcome(taskDefinition: TaskDefinition, status: TaskStatus, outcome: String): Boolean {
        val outcomeConfig = taskDefinition.outcomeConfiguration ?: return false

        // Check if outcome is in the possible outcomes list (if configured)
        val possibleOutcomes = getAllPossibleOutcomeCodesFromMap(outcomeConfig)
        if (possibleOutcomes.isNotEmpty() && !possibleOutcomes.contains(outcome)) {
            return false
        }

        // Check if outcome is valid for the current status
        return isValidOutcomeFromMap(outcomeConfig, status, outcome)
    }

    /**
     * Get valid outcomes for a specific task definition and status
     */
    fun getValidOutcomes(taskDefinitionKey: String, status: TaskStatus): List<String> {
        val taskDefinition = taskDefinitionRepository.findByKey(taskDefinitionKey)
            ?: throw TaskValidationException("error.task.definition.not.found", arrayOf(taskDefinitionKey), messageSource)

        return getValidOutcomes(taskDefinition, status)
    }

    /**
     * Get valid outcomes for a task definition and status
     */
    fun getValidOutcomes(taskDefinition: TaskDefinition, status: TaskStatus): List<String> {
        return getValidOutcomeCodesFromMap(taskDefinition.outcomeConfiguration, status)
    }

    /**
     * Get all possible outcomes for a task definition
     */
    fun getAllPossibleOutcomes(taskDefinitionKey: String): List<String> {
        val taskDefinition = taskDefinitionRepository.findByKey(taskDefinitionKey)
            ?: throw TaskValidationException("error.task.definition.not.found", arrayOf(taskDefinitionKey), messageSource)

        return getAllPossibleOutcomeCodesFromMap(taskDefinition.outcomeConfiguration)
    }

    /**
     * Validates and throws exception if outcome is invalid
     */
    fun validateOutcomeOrThrow(taskDefinitionKey: String, status: TaskStatus, outcome: String?) {
        if (outcome != null && !validateOutcome(taskDefinitionKey, status, outcome)) {
            val validOutcomes = getValidOutcomes(taskDefinitionKey, status)
            throw TaskValidationException(
                "error.task.invalid.outcome",
                arrayOf(outcome, status.value, validOutcomes.joinToString(", ")),
                messageSource
            )
        }
    }

    /**
     * Get all outcomes mapped to any status for a task definition
     */
    fun getAllMappedOutcomes(taskDefinitionKey: String): List<String> {
        val taskDefinition = taskDefinitionRepository.findByKey(taskDefinitionKey)
            ?: throw TaskValidationException("error.task.definition.not.found", arrayOf(taskDefinitionKey), messageSource)

        return getAllPossibleOutcomeCodesFromMap(taskDefinition.outcomeConfiguration)
    }

    /**
     * Validate the outcome configuration for a task definition
     */
    fun validateOutcomeConfiguration(taskDefinition: TaskDefinition): Boolean {
        return validateConfigurationFromMap(taskDefinition.outcomeConfiguration)
    }

    // Helper functions to work with Map structure
    @Suppress("UNCHECKED_CAST")
    private fun getValidOutcomeCodesFromMap(outcomeConfig: Map<String, Any>?, status: TaskStatus): List<String> {
        if (outcomeConfig == null) return emptyList()

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any> ?: return emptyList()
        val statusOutcomes = statusOutcomeMapping[status.value] as? List<Map<String, Any>> ?: return emptyList()

        return statusOutcomes.mapNotNull { outcomeMap ->
            outcomeMap["code"] as? String
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getAllPossibleOutcomeCodesFromMap(outcomeConfig: Map<String, Any>?): List<String> {
        if (outcomeConfig == null) return emptyList()

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any> ?: return emptyList()
        val allOutcomeCodes = mutableSetOf<String>()

        statusOutcomeMapping.values.forEach { statusOutcomes ->
            val outcomes = statusOutcomes as? List<Map<String, Any>> ?: return@forEach
            outcomes.forEach { outcomeMap ->
                (outcomeMap["code"] as? String)?.let { allOutcomeCodes.add(it) }
            }
        }

        return allOutcomeCodes.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun isValidOutcomeFromMap(outcomeConfig: Map<String, Any>?, status: TaskStatus, outcome: String): Boolean {
        if (outcomeConfig == null) return false

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any> ?: return false
        val statusOutcomes = statusOutcomeMapping[status.value] as? List<Map<String, Any>> ?: return false

        return statusOutcomes.any { outcomeMap ->
            outcomeMap["code"] as? String == outcome
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun validateConfigurationFromMap(outcomeConfig: Map<String, Any>?): Boolean {
        if (outcomeConfig == null) return true

        val statusOutcomeMapping = outcomeConfig["statusOutcomeMapping"] as? Map<String, Any>
        return !statusOutcomeMapping.isNullOrEmpty()
    }
}
