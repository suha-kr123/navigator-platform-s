package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTasksResponse
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import java.util.*

interface LeadTaskService {
    fun getAllTasks(paginationRequest: PaginationRequest): PaginatedResponse<LeadTasksResponse>
    fun getLeadTasks(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTasksResponse>
    fun createTaskForLead(leadId: UUID, createTaskRequest: TaskRequest): LeadTasksResponse
    fun patchTaskForLead(leadId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): LeadTasksResponse
    fun deleteTaskForLead(leadId: UUID, taskId: UUID)
}