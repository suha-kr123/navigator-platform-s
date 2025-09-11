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
import com.nivasafinance.features.lead.service.LeadService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "leadCacheManager")
class LeadServiceImpl(
    private val leadRepository: LeadRepository
) : LeadService, BaseNavigatorService() {

    @Transactional
    @CacheEvict(value = ["leads"], allEntries = true)
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
        return mapEntityToResponse(savedLead)
    }

    @Cacheable(value = ["leads"], key = "#leadId")
    override fun getLeadById(leadId: UUID): LeadResponse {
        val lead = leadRepository.findById(leadId)
            .orElseThrow { LeadNotFoundException(leadId, messageSource) }
        return mapEntityToResponse(lead)
    }

    @Transactional
    @CacheEvict(value = ["leads"], key = "#leadId")
    override fun updateLead(leadId: UUID, request: LeadUpdateRequest): LeadResponse {
        val existingLead = leadRepository.findById(leadId)
            .orElseThrow { LeadNotFoundException(leadId, messageSource) }

        val updatedLead = existingLead.copy(
            requestedAmount = request.requestedAmount ?: existingLead.requestedAmount,
            purpose = request.purpose ?: existingLead.purpose,
            productCode = request.productCode ?: existingLead.productCode,
            status = request.status ?: existingLead.status,
            stage = request.stage ?: existingLead.stage,
            preliminaryInformation = request.preliminaryInformation ?: existingLead.preliminaryInformation,
            leadContacts = request.leadContacts ?: existingLead.leadContacts,
            sourcingChannel = request.sourcingChannel ?: existingLead.sourcingChannel
        )

        val savedLead = leadRepository.save(updatedLead)
        return mapEntityToResponse(savedLead)
    }

    private fun mapEntityToResponse(lead: Lead): LeadResponse {
        return LeadResponse(
            id = lead.id!!,
            requestedAmount = lead.requestedAmount,
            purpose = lead.purpose,
            productCode = lead.productCode,
            status = lead.status,
            stage = lead.stage,
            preliminaryInformation = lead.preliminaryInformation,
            leadContacts = lead.leadContacts,
            sourcingChannel = lead.sourcingChannel,
            extData = lead.extData
        )
    }
}
