package com.nivasafinance.features.taskdefinitions.service

import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionCreateRequest
import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionUpdateRequest
import java.util.UUID

interface TaskDefinitionService {
    fun getTaskDefinition(id: UUID): TaskDefinitionResponse
    fun getTaskDefinitionByKey(key: String): TaskDefinitionResponse
    fun getAllTaskDefinitions(): List<TaskDefinitionResponse>
    fun getTaskDefinitionsByActionsGroup(actionsGroup: String): List<TaskDefinitionResponse>
    fun getTaskDefinitionsByIdentifier(identifier: String): List<TaskDefinitionResponse>
    fun createTaskDefinition(request: TaskDefinitionCreateRequest): TaskDefinitionResponse
    fun updateTaskDefinition(id: UUID, request: TaskDefinitionUpdateRequest): TaskDefinitionResponse
    fun deleteTaskDefinition(id: UUID)
    fun existsByKey(key: String): Boolean
}
