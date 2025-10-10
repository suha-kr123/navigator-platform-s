package com.nivasafinance.features.taskdefinitions.service

import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
import com.nivasafinance.features.tasks.enum.TaskStatus

interface TaskDefinitionService {
    fun getTaskOutcomesByKey(key: String, status: TaskStatus? = null): TaskOutcomesResponse
    fun getAllTaskDefinitions(): List<TaskDefinitionResponse>
}
