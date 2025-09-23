package com.nivasafinance.features.advisorleadmapping.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.UpdateAdvisorLeadMappingRequest
import java.util.UUID

interface AdvisorLeadMappingService {
    fun createAdvisorLeadMapping(advisorLeadMappingRequest: AdvisorLeadMappingRequest): AdvisorLeadMappingResponse
    fun updateAdvisorLeadMapping(
        mappingId: UUID,
        updateRequest: UpdateAdvisorLeadMappingRequest
    ): AdvisorLeadMappingResponse
    fun getAdvisorLeadMapping(mappingId: UUID): AdvisorLeadMappingResponse
    fun getAllLeadsOfAdvisor(
        advisorId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<AdvisorLeadMappingResponse>
    fun deleteAdvisorLeadMapping(mappingId: UUID)
}
