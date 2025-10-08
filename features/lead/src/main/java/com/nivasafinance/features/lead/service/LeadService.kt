package com.nivasafinance.features.lead.service

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.AddLeadPersonRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.dto.UpdateLeadPersonRequest
import java.util.UUID

interface LeadService {
    fun createLead(leadCreateRequest: LeadCreateRequest): LeadResponse
    fun getLeadById(id: UUID): LeadResponse
    fun getAllLeads(paginationRequest: PaginationRequest, search: String? = null): PaginatedResponse<LeadResponse>
    fun updateLead(id: UUID, leadUpdateRequest: LeadUpdateRequest): LeadResponse

    // Person management methods
    fun addLeadPerson(leadId: UUID, addLeadPersonRequest: AddLeadPersonRequest): LeadResponse
    fun updateLeadPerson(leadId: UUID, personId: UUID, updateLeadPersonRequest: UpdateLeadPersonRequest): LeadResponse
}
