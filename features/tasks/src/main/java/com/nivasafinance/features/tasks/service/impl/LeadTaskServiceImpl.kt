package com.nivasafinance.features.tasks.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.tasks.dto.LeadTaskResponse
import com.nivasafinance.features.tasks.enum.TaskStatus
import com.nivasafinance.features.tasks.exception.LeadTaskNotFoundException
import com.nivasafinance.features.tasks.repository.LeadTaskRepository
import com.nivasafinance.features.tasks.service.LeadTaskService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class LeadTaskServiceImpl(
    private val leadTaskRepository: LeadTaskRepository
) : LeadTaskService, BaseNavigatorService() {

    override fun getLeadTask(id: UUID): LeadTaskResponse {
        val leadTask = leadTaskRepository.findById(id)
            .orElseThrow { LeadTaskNotFoundException(id) }
        return LeadTaskResponse.fromEntity(leadTask)
    }

    override fun getAllLeadTasks(): List<LeadTaskResponse> {
        return leadTaskRepository.findAll().map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByLeadId(leadId: UUID): List<LeadTaskResponse> {
        return leadTaskRepository.findByLeadId(leadId).map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByAssignedTo(assignedTo: String): List<LeadTaskResponse> {
        return leadTaskRepository.findByAssignedTo(assignedTo).map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByStatus(status: TaskStatus): List<LeadTaskResponse> {
        return leadTaskRepository.findByStatus(status).map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByTaskKey(taskKey: String): List<LeadTaskResponse> {
        return leadTaskRepository.findByTaskKey(taskKey).map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByLeadIdAndStatus(leadId: UUID, status: TaskStatus): List<LeadTaskResponse> {
        return leadTaskRepository.findByLeadIdAndStatus(leadId, status).map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByAssignedToAndStatus(assignedTo: String, status: TaskStatus): List<LeadTaskResponse> {
        return leadTaskRepository.findByAssignedToAndStatus(assignedTo, status).map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getOverdueTasks(): List<LeadTaskResponse> {
        return leadTaskRepository.findOverdueTasks(LocalDateTime.now(), TaskStatus.COMPLETED)
            .map { LeadTaskResponse.fromEntity(it) }
    }

    override fun getLeadTasksByLeadIdAndStage(leadId: UUID, stage: String): List<LeadTaskResponse> {
        return leadTaskRepository.findByLeadIdAndStage(leadId, stage).map { LeadTaskResponse.fromEntity(it) }
    }
}
