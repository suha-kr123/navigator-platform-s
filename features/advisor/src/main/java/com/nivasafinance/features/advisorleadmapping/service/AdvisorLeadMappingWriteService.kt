package com.nivasafinance.features.advisorleadmapping.service

import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import java.util.UUID

interface AdvisorLeadMappingWriteService {

    fun createAdvisorLeadMappingData(
        advisorId: UUID,
        leadId: UUID,
        request: AdvisorLeadMappingCreateRequest
    ): AdvisorLeadMappingData

    fun updateAdvisorLeadMappingData(id: UUID, request: AdvisorLeadMappingUpdateRequest): AdvisorLeadMappingData

    fun deleteAdvisorLeadMapping(id: UUID)
}
