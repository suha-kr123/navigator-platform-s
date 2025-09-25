package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.CreateTaskForLeadRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadTaskResponse
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.TaskData
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.stages.service.StageService
import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.tasks.service.TaskService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LeadServiceImpl(
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val stageRepositoryWrapper: StageRepositoryWrapper,
    private val stageDefinitionRepositoryWrapper: StageDefinitionRepositoryWrapper,
    private val stageService: StageService,
    private val taskService: TaskService,
    private val messageSource: MessageSource
) : LeadService {

    override fun createLead(leadCreateRequest: LeadCreateRequest): LeadResponse {
        LeadExceptionFactory.validateLeadForCreation(
            leadCreateRequest.requestedAmount,
            leadCreateRequest.purpose,
            leadCreateRequest.productCode,
            null,
            leadCreateRequest.sourcingChannel,
            messageSource
        )
        val lead = toLead(leadCreateRequest)
        val savedLead = leadRepositoryWrapper.saveWithException(lead)

        val stages = createStagesForLead(savedLead.id!!, leadCreateRequest.pipelineKey)
        
        // Update the lead with the stage IDs
        val stageIds = stages.map { it.id }
        val updatedLead = savedLead.copy(stageIds = stageIds)
        val finalLead = leadRepositoryWrapper.saveWithException(updatedLead)

        return toLeadResponse(finalLead)
    }

    override fun createTaskForLead(leadId: UUID, createTaskForLeadRequest: CreateTaskForLeadRequest): LeadTaskResponse {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        
        // Create task using TaskService
        val taskRequest = com.nivasafinance.features.tasks.dto.TaskRequest(
            taskDefinitionKey = createTaskForLeadRequest.taskDefinitionKey,
            description = createTaskForLeadRequest.description,
            assignedTo = createTaskForLeadRequest.assignedTo,
            status = createTaskForLeadRequest.status,
            outcome = createTaskForLeadRequest.outcome
        )
        
        val taskResponse = taskService.createTask(taskRequest)
        
        // Update lead's task data
        val currentTaskData = lead.taskData ?: emptyList()
        val newTaskData = currentTaskData + TaskData(
            taskId = taskResponse.id
        )
        
        val updatedLead = lead.copy(taskData = newTaskData)
        leadRepositoryWrapper.saveWithException(updatedLead)
        
        return LeadTaskResponse(
            leadId = leadId,
            task = taskResponse
        )
    }

    override fun getLeadById(id: UUID): LeadResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(id)
        return toLeadResponse(lead)
    }

    override fun getAllLeads(paginationRequest: PaginationRequest): PaginatedResponse<LeadResponse> {
        val pageable = org.springframework.data.domain.PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )
        val leadPage = leadRepositoryWrapper.findAllWithException(pageable)
        val leadResponses = leadPage.content.map { toLeadResponse(it) }

        val totalPages = if (leadPage.totalElements == 0L) 0 else ((leadPage.totalElements - 1) / paginationRequest.limit + 1).toInt()
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = leadResponses,
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadPage.totalElements,
                totalPages = totalPages,
                currentPage = currentPage,
                hasNext = currentPage < totalPages - 1,
                hasPrevious = currentPage > 0
            )
        )
    }

    private fun toLead(leadCreateRequest: LeadCreateRequest): Lead {
        return Lead(
            requestedAmount = leadCreateRequest.requestedAmount,
            purpose = leadCreateRequest.purpose,
            productCode = leadCreateRequest.productCode,
            pipelineKey = leadCreateRequest.pipelineKey,
            currentStage = leadCreateRequest.currentStage,
            sourcingChannel = leadCreateRequest.sourcingChannel,
            preliminaryInformation = leadCreateRequest.preliminaryInformation?.let {
                mapOf("data" to it)
            },
            extData = leadCreateRequest.extData
        )
    }

    private fun toLeadResponse(lead: Lead): LeadResponse {
        return LeadResponse(
            id = lead.id ?: UUID.randomUUID(),
            requestedAmount = lead.requestedAmount,
            purpose = lead.purpose,
            productCode = lead.productCode,
            currentStage = lead.currentStage,
            status = null,
            preliminaryInformation = lead.preliminaryInformation?.get(
                "data"
            ) as? com.nivasafinance.features.lead.dto.LeadPreliminaryInformation,
            sourcingChannel = lead.sourcingChannel,
            extData = lead.extData,
            taskData = lead.taskData?.let { taskDataList ->
                taskDataList.associate { taskData ->
                    taskData.taskId to mapOf(
                        "documentIds" to taskData.documentIds,
                        "notesIds" to taskData.notesIds,
                        "callIds" to taskData.callIds
                    )
                }
            },
            createdAt = lead.createdAt ?: java.time.LocalDateTime.now(),
            createdBy = lead.createdBy,
            updatedAt = lead.updatedAt ?: java.time.LocalDateTime.now(),
            updatedBy = lead.updatedBy
        )
    }

    private fun createStagesForLead(leadId: UUID, pipelineKey: String?): List<com.nivasafinance.features.stages.dto.StageResponse> {
        if (pipelineKey.isNullOrBlank()) {
            return emptyList()
        }

        val stageDefinitions = stageDefinitionRepositoryWrapper.findByPipelineKeyWithException(pipelineKey)
        val stages = mutableListOf<com.nivasafinance.features.stages.dto.StageResponse>()

        stageDefinitions.forEach { stageDefinition ->
            // Use the first valid outcome from the stage definition
            val validOutcome = stageDefinition.possibleOutcomes?.firstOrNull() ?: "PENDING"
            val stageRequest = StageRequest(
                stageDefinitionKey = stageDefinition.key,
                outcome = validOutcome,
                assignedTo = null
            )
            val savedStage = stageService.createStage(stageRequest)
            stages.add(savedStage)
        }

        return stages
    }

    override fun getLeadIdByTaskId(taskId: UUID): UUID? {
        // Get all leads and find the one that contains the taskId
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10000)
        val allLeadsPage = leadRepositoryWrapper.findAllWithException(pageable)
        val allLeads = allLeadsPage.content

        return allLeads.find { lead ->
            lead.taskData?.any { taskData -> taskData.taskId == taskId } == true
        }?.id
    }

    override fun getLeadTasks(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskResponse> {
        // First get the lead to check if it exists and get its taskData
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If lead has no tasks, return empty paginated response
        if (lead.taskData.isNullOrEmpty()) {
            return PaginatedResponse(
                content = emptyList(),
                pagination = PaginationInfo(
                    offset = paginationRequest.offset,
                    limit = paginationRequest.limit,
                    totalElements = 0,
                    totalPages = 0,
                    currentPage = 0,
                    hasNext = false,
                    hasPrevious = false
                )
            )
        }

        // Get all tasks and filter by the taskIds from the lead
        val allTasks = taskService.getAllTasks(paginationRequest)
        val leadTaskIds = lead.taskData!!.map { taskData -> taskData.taskId }.toSet()
        val filteredTasks = allTasks.content.filter { task ->
            leadTaskIds.contains(task.id)
        }

        // Convert to LeadTaskResponse with lead ID
        val leadTaskResponses = filteredTasks.map { task ->
            LeadTaskResponse(
                leadId = leadId,
                task = task
            )
        }

        // Create a new paginated response with filtered tasks
        return PaginatedResponse(
            content = leadTaskResponses,
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadTaskResponses.size.toLong(),
                totalPages = if (leadTaskResponses.isEmpty()) 0 else 1,
                currentPage = 0,
                hasNext = false,
                hasPrevious = false
            )
        )
    }

    override fun getAllLeadsTasks(paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskResponse> {
        // Get all leads to collect all taskData - use a large page size to get all leads
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10000)
        val allLeadsPage = leadRepositoryWrapper.findAllWithException(pageable)
        val allLeads = allLeadsPage.content

        // If no leads found, return empty paginated response
        if (allLeads.isEmpty()) {
            return PaginatedResponse(
                content = emptyList(),
                pagination = PaginationInfo(
                    offset = paginationRequest.offset,
                    limit = paginationRequest.limit,
                    totalElements = 0,
                    totalPages = 0,
                    currentPage = 0,
                    hasNext = false,
                    hasPrevious = false
                )
            )
        }

        // Get all tasks
        val allTasks = taskService.getAllTasks(paginationRequest)

        // Create a map of taskId to leadId for quick lookup
        val taskToLeadMap = mutableMapOf<UUID, UUID>()
        allLeads.forEach { lead ->
            lead.taskData?.forEach { taskData ->
                taskToLeadMap[taskData.taskId] = lead.id!!
            }
        }

        // Filter tasks that belong to leads and create LeadTaskResponse
        val leadTaskResponses = allTasks.content.filter { task ->
            taskToLeadMap.containsKey(task.id)
        }.map { task ->
            LeadTaskResponse(
                leadId = taskToLeadMap[task.id]!!,
                task = task
            )
        }

        // Create a new paginated response with filtered tasks
        return PaginatedResponse(
            content = leadTaskResponses,
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadTaskResponses.size.toLong(),
                totalPages = if (leadTaskResponses.isEmpty()) 0 else 1,
                currentPage = 0,
                hasNext = false,
                hasPrevious = false
            )
        )
    }
}
