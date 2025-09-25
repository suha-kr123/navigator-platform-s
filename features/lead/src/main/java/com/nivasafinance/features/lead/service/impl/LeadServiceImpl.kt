package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.stages.service.StageService
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

    override fun updateLead(id: UUID, leadUpdateRequest: com.nivasafinance.features.lead.dto.LeadUpdateRequest): LeadResponse {
        val existingLead = leadRepositoryWrapper.findByIdWithException(id)

        // Create a mutable copy to update only provided fields
        val updatedLead = existingLead.copy(
            requestedAmount = leadUpdateRequest.requestedAmount ?: existingLead.requestedAmount,
            purpose = leadUpdateRequest.purpose ?: existingLead.purpose,
            productCode = leadUpdateRequest.productCode ?: existingLead.productCode,
            pipelineKey = leadUpdateRequest.pipelineKey ?: existingLead.pipelineKey,
            currentStage = leadUpdateRequest.currentStage ?: existingLead.currentStage,
            sourcingChannel = leadUpdateRequest.sourcingChannel ?: existingLead.sourcingChannel,
            preliminaryInformation = if (leadUpdateRequest.preliminaryInformation != null) {
                mapOf("data" to leadUpdateRequest.preliminaryInformation)
            } else {
                existingLead.preliminaryInformation
            },
            extData = leadUpdateRequest.extData ?: existingLead.extData
        )

        val savedLead = leadRepositoryWrapper.saveWithException(updatedLead)
        return toLeadResponse(savedLead)
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
}
