package com.nivasafinance.features.taskdefinitions.service

import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse

interface TaskDefinitionService {
    fun getTaskOutcomesByKey(key: String): TaskOutcomesResponse
}
