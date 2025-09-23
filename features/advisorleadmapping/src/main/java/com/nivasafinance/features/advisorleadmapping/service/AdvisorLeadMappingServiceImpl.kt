package com.nivasafinance.features.advisorleadmapping.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.UpdateAdvisorLeadMappingRequest
import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import com.nivasafinance.features.advisorleadmapping.repository.AdvisorLeadMappingRepositoryWrapper
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AdvisorLeadMappingServiceImpl(
    private val advisorLeadMappingRepositoryWrapper: AdvisorLeadMappingRepositoryWrapper,
    private val advisorRepositoryWrapper: AdvisorRepositoryWrapper,
    private val leadRepositoryWrapper: LeadRepositoryWrapper
) : AdvisorLeadMappingService {

    override fun createAdvisorLeadMapping(
        advisorLeadMappingRequest: AdvisorLeadMappingRequest
    ): AdvisorLeadMappingResponse {
        // Validate advisor exists
        val advisorId = advisorLeadMappingRequest.advisorId
            ?: throw IllegalArgumentException("advisorId is required")

        runCatching { advisorRepositoryWrapper.findByIdWithException(advisorId) }
            .getOrElse { throw IllegalArgumentException("Advisor with ID $advisorId not found") }

        // Validate lead exists
        val leadId = advisorLeadMappingRequest.leadId
            ?: throw IllegalArgumentException("leadId is required")

        runCatching { leadRepositoryWrapper.findByIdWithException(leadId) }
            .getOrElse { throw IllegalArgumentException("Lead with ID $leadId not found") }

        val advisorLeadMapping = AdvisorLeadMapping(
            advisorId = advisorLeadMappingRequest.advisorId,
            leadId = advisorLeadMappingRequest.leadId,
            verificationStatus = advisorLeadMappingRequest.verificationStatus,
            verificationNotes = advisorLeadMappingRequest.verificationNotes,
            extData = advisorLeadMappingRequest.extData
        )

        val savedMapping = advisorLeadMappingRepositoryWrapper.saveWithException(advisorLeadMapping)
        return toAdvisorLeadMappingResponse(savedMapping)
    }

    override fun updateAdvisorLeadMapping(
        mappingId: UUID,
        updateRequest: UpdateAdvisorLeadMappingRequest
    ): AdvisorLeadMappingResponse {
        val existingMapping = advisorLeadMappingRepositoryWrapper.findByIdWithException(mappingId)

        // Update fields directly (only if provided)
        updateRequest.verificationStatus?.let { existingMapping.verificationStatus = it }
        updateRequest.verificationNotes?.let { existingMapping.verificationNotes = it }
        updateRequest.extData?.let { existingMapping.extData = it }

        val savedMapping = advisorLeadMappingRepositoryWrapper.saveWithException(existingMapping)
        return toAdvisorLeadMappingResponse(savedMapping)
    }

    override fun getAdvisorLeadMapping(mappingId: UUID): AdvisorLeadMappingResponse {
        val mapping = advisorLeadMappingRepositoryWrapper.findByIdWithException(mappingId)
        return toAdvisorLeadMappingResponse(mapping)
    }

    override fun getAllLeadsOfAdvisor(
        advisorId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<AdvisorLeadMappingResponse> {
        // Validate advisor exists
        runCatching { advisorRepositoryWrapper.findByIdWithException(advisorId) }
            .getOrElse { throw IllegalArgumentException("Advisor with ID $advisorId not found") }

        val sort = Sort.by(
            if (paginationRequest.sortDirection.name == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
            paginationRequest.sortBy
        )

        val pageable = PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit,
            sort
        )

        val mappingPage = advisorLeadMappingRepositoryWrapper.findAllByAdvisorIdWithException(advisorId, pageable)

        val mappingResponses = mappingPage.content.map { mapping ->
            toAdvisorLeadMappingResponse(mapping)
        }

        val paginationInfo = PaginationInfo(
            offset = paginationRequest.offset,
            limit = paginationRequest.limit,
            totalElements = mappingPage.totalElements,
            totalPages = mappingPage.totalPages,
            currentPage = mappingPage.number,
            hasNext = mappingPage.hasNext(),
            hasPrevious = mappingPage.hasPrevious()
        )

        return PaginatedResponse(
            content = mappingResponses,
            pagination = paginationInfo
        )
    }

    override fun deleteAdvisorLeadMapping(mappingId: UUID) {
        advisorLeadMappingRepositoryWrapper.findByIdWithException(mappingId)
        advisorLeadMappingRepositoryWrapper.deleteByIdWithException(mappingId)
    }

    private fun toAdvisorLeadMappingResponse(mapping: AdvisorLeadMapping): AdvisorLeadMappingResponse {
        return AdvisorLeadMappingResponse(
            id = checkNotNull(mapping.id) { "AdvisorLeadMapping ID cannot be null" },
            advisorId = mapping.advisorId,
            leadId = mapping.leadId,
            verificationStatus = mapping.verificationStatus,
            verificationNotes = mapping.verificationNotes,
            extData = mapping.extData,
            createdAt = mapping.createdAt ?: java.time.LocalDateTime.now(),
            createdBy = mapping.createdBy,
            updatedAt = mapping.updatedAt ?: java.time.LocalDateTime.now(),
            updatedBy = mapping.updatedBy
        )
    }
}
