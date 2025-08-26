package com.nivasafinance.features.advisorleadmapping.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingNotFoundException
import com.nivasafinance.features.advisorleadmapping.repository.AdvisorLeadMappingRepository
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorLeadMappingReadServiceImpl(
    private val advisorLeadMappingRepository: AdvisorLeadMappingRepository,
    private val advisorService: AdvisorService
) : AdvisorLeadMappingReadService, BaseNavigatorService() {

    override fun getAdvisorLeadMappingData(id: UUID): AdvisorLeadMappingData {
        val mapping = advisorLeadMappingRepository.findById(id)
            .orElseThrow { AdvisorLeadMappingNotFoundException(id, messageSource) }
        return AdvisorLeadMappingData.fromEntity(mapping)
    }

    override fun getAdvisorLeadMappingDataByAdvisorAndLead(advisorId: UUID, leadId: UUID): AdvisorLeadMappingData? {
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
        return mapping?.let { AdvisorLeadMappingData.fromEntity(it) }
    }

    override fun getAllLeadsForAdvisorData(advisorId: UUID): List<AdvisorLeadMappingData> {
        val allMappings = advisorLeadMappingRepository.findByAdvisorId(advisorId)
        return allMappings.map { mapping ->
            AdvisorLeadMappingData.fromEntity(mapping)
        }
    }

    override fun getAllLeadsForAdvisorByMobileData(mobileNumber: String): List<AdvisorLeadMappingData> {
        val advisor = advisorService.getAdvisorByMobileNo(mobileNumber)
        return getAllLeadsForAdvisorData(advisor.id)
    }
}
