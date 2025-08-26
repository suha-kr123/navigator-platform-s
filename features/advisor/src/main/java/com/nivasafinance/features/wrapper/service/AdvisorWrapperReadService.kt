package com.nivasafinance.features.wrapper.service

import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import java.util.UUID

interface AdvisorWrapperReadService {
    fun getAdvisor(advisorId: UUID): AdvisorWrapperResponse
}
