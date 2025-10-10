package com.nivasafinance.features.tasks.dto

/**
 * Represents a task outcome with name, code, and description.
 * This provides a structured way to define outcomes with meaningful names and descriptions.
 */
data class TaskOutcome(
    val name: String,
    val code: String,
    val description: String
) {
    /**
     * Get the outcome as a string (returns the code for backward compatibility)
     */
    fun asString(): String = code

    companion object {
        /**
         * Create a TaskOutcome from a string (for backward compatibility)
         */
        fun fromString(outcome: String): TaskOutcome {
            val name = outcome.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            return TaskOutcome(
                name = name,
                code = outcome,
                description = name
            )
        }

        /**
         * Create a TaskOutcome with all fields
         */
        fun create(
            name: String,
            code: String,
            description: String
        ): TaskOutcome {
            return TaskOutcome(
                name = name,
                code = code,
                description = description
            )
        }

        /**
         * Create a list of TaskOutcomes from a list of strings
         */
        fun fromStrings(outcomes: List<String>): List<TaskOutcome> {
            return outcomes.map { fromString(it) }
        }
    }
}
