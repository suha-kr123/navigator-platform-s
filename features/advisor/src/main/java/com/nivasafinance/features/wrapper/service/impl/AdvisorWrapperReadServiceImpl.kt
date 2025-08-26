package com.nivasafinance.features.wrapper.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.service.AdvisorWrapperReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorWrapperReadServiceImpl(
    private val advisorService: AdvisorService,
    private val advisorLeadMappingService: AdvisorLeadMappingService,
    private val leadService: LeadService
) : AdvisorWrapperReadService, BaseNavigatorService() {

    override fun getAdvisor(advisorId: UUID): AdvisorWrapperResponse {
        val advisorResponse = advisorService.getAdvisor(advisorId)
        val leads = advisorLeadMappingService.getAllLeadsForAdvisor(advisorId)

        return AdvisorWrapperResponse(
            id = advisorResponse.id,
            name = "${advisorResponse.personalDetails.firstName.orEmpty()} " +
                "${advisorResponse.personalDetails.lastName.orEmpty()}".trim(),
            status = advisorResponse.status
                ?: com.nivasafinance.features.advisor.enum.AdvisorStatus.CREATED,
            leads = leads.map { mapping ->
                val lead = leadService.getLeadById(mapping.leadId)
                AdvisorWrapperResponse.LeadInfo(
                    leadId = mapping.leadId,
                    status = lead.status ?: com.nivasafinance.features.lead.enum.LeadStatus.ACTIVE,
                    stage = lead.stage ?: com.nivasafinance.features.lead.enum.LeadStage.INQUIRY
                )
            }
        )
    }
}
