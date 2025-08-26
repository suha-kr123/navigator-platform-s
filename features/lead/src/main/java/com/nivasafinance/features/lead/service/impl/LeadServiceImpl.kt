package com.nivasafinance.features.lead.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.service.LeadService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class LeadServiceImpl(
    private val leadReadService: com.nivasafinance.features.lead.service.LeadReadService,
    private val leadWriteService: com.nivasafinance.features.lead.service.LeadWriteService
) : LeadService, BaseNavigatorService() {

    @CacheEvict(value = ["leads"], allEntries = true)
    override fun createLead(request: LeadCreateRequest): LeadResponse {
        return leadWriteService.createLead(request)
    }

    @Cacheable(value = ["leads"], key = "#leadId")
    override fun getLeadById(leadId: java.util.UUID): LeadResponse {
        return leadReadService.getLeadById(leadId)
    }

    @CacheEvict(value = ["leads"], key = "#leadId")
    override fun updateLead(leadId: java.util.UUID, request: LeadUpdateRequest): LeadResponse {
        return leadWriteService.updateLead(leadId, request)
    }
}
