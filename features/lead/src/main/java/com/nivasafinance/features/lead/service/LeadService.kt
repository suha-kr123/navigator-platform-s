package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.CreateTaskForLeadRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadTaskResponse
import java.util.UUID

interface LeadService {
    fun createLead(leadCreateRequest: LeadCreateRequest): LeadResponse
    fun getLeadById(id: UUID): LeadResponse
    fun getAllLeads(paginationRequest: PaginationRequest): PaginatedResponse<LeadResponse>
    fun createTaskForLead(leadId: UUID, createTaskForLeadRequest: CreateTaskForLeadRequest): LeadTaskResponse
    fun getLeadTasks(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskResponse>
    fun getAllLeadsTasks(paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskResponse>
    fun getLeadIdByTaskId(taskId: UUID): UUID?
}
