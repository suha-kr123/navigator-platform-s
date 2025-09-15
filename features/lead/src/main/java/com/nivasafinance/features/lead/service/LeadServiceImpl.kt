package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
// Stage and task functionality temporarily disabled
// import com.nivasafinance.features.stages.entity.Stage
// import com.nivasafinance.features.tasks.entity.Task
// import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
// import com.nivasafinance.features.tasks.repository.TaskRepositoryWrapper
// import com.nivasafinance.features.stages.dto.StageResponse
// import com.nivasafinance.features.tasks.dto.TaskResponse
// import com.nivasafinance.features.stages.enum.EntityType as StageEntityType
// import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
// import com.nivasafinance.features.stagedefinitions.entity.StageDefinition
// import com.nivasafinance.features.stagedefinitions.dto.StageDefinitionResponse
// import com.nivasafinance.features.stages.enum.Status
// import com.nivasafinance.features.stages.enum.Outcome

@Service
@Transactional
class LeadServiceImpl(
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    // private val stageRepositoryWrapper: StageRepositoryWrapper,
    // private val taskRepositoryWrapper: TaskRepositoryWrapper,
    // private val stageDefinitionRepositoryWrapper: StageDefinitionRepositoryWrapper,
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
            status = null,
            preliminaryInformation = leadCreateRequest.preliminaryInformation,
            leadContacts = leadCreateRequest.leadContacts,
            sourcingChannel = leadCreateRequest.sourcingChannel,
            extData = leadCreateRequest.extData
        )
    }

    private fun toLeadResponse(lead: Lead): LeadResponse {
        return LeadResponse(
            id = lead.id ?: UUID.randomUUID(),
            requestedAmount = lead.requestedAmount,
            purpose = lead.purpose,
            productCode = lead.productCode,
            status = lead.status,
            preliminaryInformation = lead.preliminaryInformation,
            leadContacts = lead.leadContacts,
            sourcingChannel = lead.sourcingChannel,
            extData = lead.extData,
            // stages = emptyList(), // Stages disabled for now
            createdAt = lead.createdAt ?: java.time.LocalDateTime.now(),
            createdBy = lead.createdBy,
            updatedAt = lead.updatedAt ?: java.time.LocalDateTime.now(),
            updatedBy = lead.updatedBy
        )
    }

    // Stage and task functionality removed for now
}
