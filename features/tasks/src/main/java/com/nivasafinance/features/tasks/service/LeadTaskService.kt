package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.dto.LeadTaskResponse
import com.nivasafinance.features.tasks.enum.TaskStatus
import java.util.UUID

interface LeadTaskService {
    fun getLeadTask(id: UUID): LeadTaskResponse
    fun getAllLeadTasks(): List<LeadTaskResponse>
    fun getLeadTasksByLeadId(leadId: UUID): List<LeadTaskResponse>
    fun getLeadTasksByAssignedTo(assignedTo: String): List<LeadTaskResponse>
    fun getLeadTasksByStatus(status: TaskStatus): List<LeadTaskResponse>
    fun getLeadTasksByTaskKey(taskKey: String): List<LeadTaskResponse>
    fun getLeadTasksByLeadIdAndStatus(leadId: UUID, status: TaskStatus): List<LeadTaskResponse>
    fun getLeadTasksByAssignedToAndStatus(assignedTo: String, status: TaskStatus): List<LeadTaskResponse>
    fun getOverdueTasks(): List<LeadTaskResponse>
    fun getLeadTasksByLeadIdAndStage(leadId: UUID, stage: String): List<LeadTaskResponse>
}
