package com.nivasafinance.features.advisor.service

import com.nivasafinance.features.advisor.dto.AdvisorDto
import java.util.UUID

interface AdvisorReadService {
    fun getAdvisor(id: UUID): AdvisorDto
    fun getAdvisorByMobileNo(mobileNo: String): AdvisorDto
}
