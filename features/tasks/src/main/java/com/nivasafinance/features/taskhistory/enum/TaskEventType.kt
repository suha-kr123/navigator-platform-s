package com.nivasafinance.features.taskhistory.enum

/**
 * Enum for task event types to ensure data integrity.
 * This prevents users from inserting invalid event types.
 */
enum class TaskEventType(val value: String, val description: String) {
    CREATED("CREATED", "Task was created"),
    ASSIGNED("ASSIGNED", "Task was assigned to a user"),
    STATUS_CHANGED("STATUS_CHANGED", "Task status was changed"),
    OUTCOME_SET("OUTCOME_SET", "Task outcome was set"),
    DESCRIPTION_UPDATED("DESCRIPTION_UPDATED", "Task description was updated"),
    RESCHEDULED("RESCHEDULED", "Task was rescheduled"),
    COMPLETED("COMPLETED", "Task was completed");

    companion object {
        /**
         * Get TaskEventType by value
         */
        fun fromValue(value: String): TaskEventType? {
            return values().find { it.value == value }
        }

        /**
         * Check if a value is valid
         */
        fun isValid(value: String): Boolean {
            return fromValue(value) != null
        }
    }
}
