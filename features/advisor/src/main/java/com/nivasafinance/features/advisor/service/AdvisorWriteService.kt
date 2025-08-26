package com.nivasafinance.features.advisor.service

import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorData
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import java.util.UUID

interface AdvisorWriteService {

    fun createAdvisorData(request: AdvisorCreateRequest): AdvisorData
    fun deleteAdvisor(id: UUID)
    fun updateAdvisorData(id: UUID, request: AdvisorUpdateRequest): AdvisorData
}
