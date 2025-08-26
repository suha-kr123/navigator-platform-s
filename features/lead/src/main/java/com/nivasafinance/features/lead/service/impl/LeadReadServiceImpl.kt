package com.nivasafinance.features.lead.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.repository.LeadRepository
import com.nivasafinance.features.lead.service.LeadReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadReadServiceImpl(
    private val leadRepository: LeadRepository,
) : LeadReadService, BaseNavigatorService() {

    override fun getLeadById(leadId: UUID): LeadResponse {
        val lead = leadRepository.findById(leadId)
            .orElseThrow { LeadNotFoundException(leadId, messageSource) }

        return LeadResponse(
            id = lead.id!!,
            requestedAmount = lead.requestedAmount,
            purpose = lead.purpose,
            productCode = lead.productCode,
            status = lead.status,
            stage = lead.stage,
            preliminaryInformation = lead.preliminaryInformation,
            leadContacts = lead.leadContacts,
            sourcingChannel = lead.sourcingChannel
        )
    }
}
