package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import java.util.UUID

interface LeadService {
    fun createLead(request: LeadCreateRequest): LeadResponse
    fun getLeadById(leadId: UUID): LeadResponse
    fun updateLead(leadId: UUID, request: LeadUpdateRequest): LeadResponse
}
