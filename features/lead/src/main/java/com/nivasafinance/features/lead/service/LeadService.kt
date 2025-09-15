package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import java.util.UUID
import base.model.PaginatedResponse
import base.model.PaginationRequest


interface LeadService {
    fun createLead(leadCreateRequest: LeadCreateRequest): LeadResponse
    fun getLeadById(id: UUID): LeadResponse
    fun getAllLeads(paginationRequest: PaginationRequest): PaginatedResponse<LeadResponse>
}