package com.nivasafinance.features.wrapper.service

import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import java.util.UUID

interface AdvisorWrapperService {
    fun createAdvisor(request: AdvisorWrapperRequest): AdvisorWrapperResponse
    fun getAdvisor(advisorId: UUID): AdvisorWrapperResponse
    fun createLeadForAdvisor(advisorId: UUID, request: CreateLeadForAdvisorRequest): AdvisorWrapperResponse
}
