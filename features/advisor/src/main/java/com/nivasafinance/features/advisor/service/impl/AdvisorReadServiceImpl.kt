package com.nivasafinance.features.advisor.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.dto.AdvisorData
import com.nivasafinance.features.advisor.exception.AdvisorMobileNotFoundException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorReadServiceImpl(
    private val advisorRepository: AdvisorRepository,
) : AdvisorReadService, BaseNavigatorService() {

    override fun getAdvisorData(id: UUID): AdvisorData {
        val advisor = advisorRepository.findById(id).orElseThrow {
            AdvisorNotFoundException(id, messageSource)
        }
        return AdvisorData.fromEntity(advisor)
    }

    override fun getAdvisorDataByMobileNo(mobileNo: String): AdvisorData {
        val advisor = advisorRepository.findByPrimaryMobileNo(mobileNo)
        if (advisor == null) {
            throw AdvisorMobileNotFoundException(mobileNo, messageSource)
        }
        return AdvisorData.fromEntity(advisor)
    }
}
