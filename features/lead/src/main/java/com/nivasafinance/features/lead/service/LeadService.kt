package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.AddLeadPersonRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadPersonsResponse
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
    fun getAllLeadPersons(paginationRequest: PaginationRequest, search: String? = null): List<LeadPersonsResponse>
}
