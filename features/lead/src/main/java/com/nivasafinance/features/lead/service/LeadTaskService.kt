package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.*
import java.util.*

interface LeadTaskService {
    fun createTaskForLead(leadId: UUID, stageKey: String, request: CreateTaskRequest): TaskResponse
    fun updateTaskForLead(leadId: UUID, taskId: UUID, request: UpdateTaskRequest): TaskResponse
    fun deleteTaskForLead(leadId: UUID, taskId: UUID)
    fun getTasksForLead(leadId: UUID): List<TaskResponse>
    fun getTasksForLeadAndStage(leadId: UUID, stageKey: String): List<TaskResponse>
    fun getLeadsForStage(stageKey: String): List<LeadResponse>
    fun getLeadsForStageAndPipeline(stageKey: String, pipelineKey: String): List<LeadResponse>
}
