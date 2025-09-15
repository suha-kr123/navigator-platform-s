package com.nivasafinance.features.stagedefinitions.dto

import com.nivasafinance.features.stagedefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.stagedefinitions.dto.ActionGroupResponse
import java.time.LocalDateTime

import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.stagedefinitions.dto.OutcomeResponse

data class StageDefinitionResponse(
    val key: String,
    val name: String,
    val description: String?,
    val pipelineKey: String,
    val assignmentStrategy: AssignmentStrategy,
    val actionGroups: List<ActionGroupResponse>,
    val defaultTasks: List<TaskResponse>,
    val tasksAllowed: List<TaskResponse>,
    val outcomes: List<OutcomeResponse>,
    val extData: Map<String, Any>? = null,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)