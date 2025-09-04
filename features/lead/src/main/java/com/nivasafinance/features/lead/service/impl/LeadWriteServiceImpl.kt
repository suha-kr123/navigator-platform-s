package com.nivasafinance.features.lead.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.repository.LeadRepository
import com.nivasafinance.features.lead.service.LeadWriteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LeadWriteServiceImpl(
    private val leadRepository: LeadRepository
) : LeadWriteService, BaseNavigatorService() {

    @Transactional
    override fun createLead(request: LeadCreateRequest): LeadResponse {
        val lead = Lead(
            requestedAmount = request.requestedAmount,
            purpose = request.purpose,
            productCode = request.productCode,
            status = request.status ?: LeadStatus.ACTIVE,
            stage = request.stage ?: LeadStage.INQUIRY,
            preliminaryInformation = request.preliminaryInformation,
            leadContacts = request.leadContacts,
            sourcingChannel = request.sourcingChannel,
            extData = request.extData
        )

        val savedLead = leadRepository.save(lead)
        return LeadResponse(
            id = savedLead.id!!,
            requestedAmount = savedLead.requestedAmount,
            purpose = savedLead.purpose,
            productCode = savedLead.productCode,
            status = savedLead.status,
            stage = savedLead.stage,
            preliminaryInformation = savedLead.preliminaryInformation,
            leadContacts = savedLead.leadContacts,
            sourcingChannel = savedLead.sourcingChannel,
            extData = savedLead.extData
        )
    }

    @Transactional
    override fun updateLead(leadId: UUID, request: LeadUpdateRequest): LeadResponse {
        val lead = leadRepository.findById(leadId)
            .orElseThrow { LeadNotFoundException(leadId, messageSource) }

        request.requestedAmount?.let { lead.requestedAmount = it }
        request.purpose?.let { lead.purpose = it }
        request.productCode?.let { lead.productCode = it }
        request.sourcingChannel?.let { lead.sourcingChannel = it }
        request.preliminaryInformation?.let { info ->
            lead.preliminaryInformation = info
        }
        request.stage?.let { lead.stage = it }
        request.status?.let { lead.status = it }
        request.leadContacts?.let { lead.leadContacts = it }

        val savedLead = leadRepository.save(lead)
        return LeadResponse(
            id = savedLead.id!!,
            requestedAmount = savedLead.requestedAmount,
            purpose = savedLead.purpose,
            productCode = savedLead.productCode,
            status = savedLead.status,
            stage = savedLead.stage,
            preliminaryInformation = savedLead.preliminaryInformation,
            leadContacts = savedLead.leadContacts,
            sourcingChannel = savedLead.sourcingChannel,
            extData = savedLead.extData
        )
    }
}
