package com.nivasafinance.features.lead.service

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTasksResponse
import com.nivasafinance.features.taskhistory.dto.TaskHistoryResponse
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import java.util.UUID

interface LeadTaskService {
    fun getAllTasks(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTasksResponse>>
    fun getLeadTasks(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTasksResponse>>
    fun getTaskForLead(leadId: UUID, taskId: UUID): LeadTasksResponse
    fun createTaskForLead(leadId: UUID, createTaskRequest: TaskRequest): LeadTasksResponse
    fun patchTaskForLead(leadId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): LeadTasksResponse
    fun deleteTaskForLead(leadId: UUID, taskId: UUID)
    fun getTaskHistoryForLead(
        leadId: UUID,
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<TaskHistoryResponse>
}
