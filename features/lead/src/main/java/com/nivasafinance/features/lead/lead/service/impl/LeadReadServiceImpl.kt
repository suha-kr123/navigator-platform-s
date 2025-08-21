package com.nivasafinance.features.lead.lead.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.lead.dto.LeadResponse
import com.nivasafinance.features.lead.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.lead.repository.LeadRepository
import com.nivasafinance.features.lead.lead.service.LeadReadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LeadReadServiceImpl(
    private val leadRepository: LeadRepository,
) : LeadReadService, BaseNavigatorService() {

    @Transactional
    override fun getLeadById(leadId: UUID): LeadResponse {
        val lead = leadRepository.findById(leadId)
            .orElseThrow { LeadNotFoundException(leadId, messageSource) }

        return LeadResponse(
            id = lead.id,
            stage = lead.stage.toString(),
            status = lead.status.toString()
        )
    }
}
