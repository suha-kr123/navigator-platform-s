package com.nivasafinance.features.advisor.service

import com.nivasafinance.features.advisor.dto.AdvisorData
import java.util.UUID

interface AdvisorReadService {
    fun getAdvisorData(id: UUID): AdvisorData
    fun getAdvisorDataByMobileNo(mobileNo: String): AdvisorData
}
