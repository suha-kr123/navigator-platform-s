package com.nivasafinance.features.wrapper.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.applicant.service.ApplicantService
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import com.nivasafinance.features.wrapper.service.ApplicantWrapperReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ApplicantWrapperReadServiceImpl(

    private val leadService: LeadService,
    private val applicantService: ApplicantService
) : ApplicantWrapperReadService, BaseNavigatorService() {

    override fun getApplicant(applicantId: UUID): ApplicantWrapperResponse {
        val applicant = applicantService.getApplicant(applicantId)
        val lead = leadService.getLeadById(applicant.leadId)

        return ApplicantWrapperResponse(
            id = applicant.id!!,
            status = lead.status ?: com.nivasafinance.features.lead.enum.LeadStatus.ACTIVE,
            stage = lead.stage ?: com.nivasafinance.features.lead.enum.LeadStage.INQUIRY
        )
    }
}
