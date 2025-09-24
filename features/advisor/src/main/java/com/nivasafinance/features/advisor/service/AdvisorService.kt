package com.nivasafinance.features.advisor.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.advisor.dto.AdvisorRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import java.util.UUID

interface AdvisorService {
    fun createAdvisor(advisorRequest: AdvisorRequest): AdvisorResponse
    fun updateAdvisor(advisorId: UUID, updateAdvisorRequest: AdvisorUpdateRequest): AdvisorResponse
    fun getAdvisor(advisorId: UUID): AdvisorResponse
    fun getAllAdvisors(paginationRequest: PaginationRequest): PaginatedResponse<AdvisorResponse>
    fun deleteAdvisor(advisorId: UUID)
}
