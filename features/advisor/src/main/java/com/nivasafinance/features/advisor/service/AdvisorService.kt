package com.nivasafinance.features.advisor.service

import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import java.util.UUID

interface AdvisorService {
    fun getAdvisor(id: UUID): AdvisorResponse
    fun getAdvisorByMobileNo(mobileNo: String): AdvisorResponse
    fun createAdvisor(request: AdvisorCreateRequest): AdvisorResponse
    fun updateAdvisor(id: UUID, request: AdvisorUpdateRequest): AdvisorResponse
    fun deleteAdvisor(id: UUID)
}
