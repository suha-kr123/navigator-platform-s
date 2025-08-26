package com.nivasafinance.features.advisorleadmapping.service

import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData
import java.util.UUID

interface AdvisorLeadMappingReadService {

    fun getAdvisorLeadMappingData(id: UUID): AdvisorLeadMappingData

    fun getAdvisorLeadMappingDataByAdvisorAndLead(advisorId: UUID, leadId: UUID): AdvisorLeadMappingData?

    fun getAllLeadsForAdvisorData(advisorId: UUID): List<AdvisorLeadMappingData>

    fun getAllLeadsForAdvisorByMobileData(mobileNumber: String): List<AdvisorLeadMappingData>
}
