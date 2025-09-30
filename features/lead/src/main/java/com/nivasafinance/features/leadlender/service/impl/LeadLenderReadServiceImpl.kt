package com.nivasafinance.features.leadlender.service.impl

import com.nivasafinance.features.leadlender.dto.LeadLenderResponse
import com.nivasafinance.features.leadlender.entity.LeadLender
import com.nivasafinance.features.leadlender.repository.LeadLenderRepositoryWrapper
import com.nivasafinance.features.leadlender.service.LeadLenderReadService
import com.nivasafinance.features.lender.lender.service.LenderReadService
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadLenderReadServiceImpl(
    private val leadLenderRepositoryWrapper: LeadLenderRepositoryWrapper,
    private val lenderReadService: LenderReadService,
    private val lenderOfficeReadService: LenderOfficeReadService,
) : LeadLenderReadService {

    override fun getLeadLenders(leadId: UUID): List<LeadLenderResponse> {
        val leadLenders = leadLenderRepositoryWrapper.findByLeadId(leadId)
        return leadLenders.map { leadLender ->
            createLeadLenderResponse(leadLender)
        }
    }

    override fun getLeadLenderById(leadLenderId: UUID): LeadLenderResponse {
        val leadLender = leadLenderRepositoryWrapper.findByIdWithException(leadLenderId)
        return createLeadLenderResponse(leadLender)
    }

    private fun createLeadLenderResponse(
        leadLender: LeadLender
    ): LeadLenderResponse {
        val lender = lenderReadService.getByKey(leadLender.lenderKey)

        val lenderOffice = leadLender.lenderOfficeKey?.let { officeKey ->
            lenderOfficeReadService.getByKey(officeKey)
        }

        return LeadLenderResponse(
            id = leadLender.id!!,
            leadId = leadLender.leadId,
            status = leadLender.status,
            loginId = leadLender.loginId,
            lender = lender,
            lenderOffice = lenderOffice,
            relationshipManager = leadLender.rmDetails
        )
    }
}
