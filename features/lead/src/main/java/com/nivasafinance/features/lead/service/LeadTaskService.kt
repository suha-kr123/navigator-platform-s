package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.CreateTaskRequest
import com.nivasafinance.features.lead.dto.CreateTaskResponse
import java.util.*

interface LeadTaskService {
    fun createTaskForLead(leadId: UUID, request: CreateTaskRequest): CreateTaskResponse
    fun getTasksForLead(leadId: UUID): List<CreateTaskResponse>
    fun getTasksForLeadAndStage(leadId: UUID, stageKey: String): List<CreateTaskResponse>
}
