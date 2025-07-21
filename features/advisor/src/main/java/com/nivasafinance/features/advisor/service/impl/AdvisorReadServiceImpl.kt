package com.nivasafinance.features.advisor.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.exception.AdvisorMobileNotFoundException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.person.service.PersonReadService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorReadServiceImpl(
    private val personReadService: PersonReadService,
    private val advisorRepository: AdvisorRepository,
) : AdvisorReadService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAdvisor(id: UUID): AdvisorDto {
        val advisor = advisorRepository.findById(id).orElseThrow {
            AdvisorNotFoundException(id, messageSource)
        }
        return getAdvisorDTOWithPersonDetails(advisor)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #mobileNo")
    override fun getAdvisorByMobileNo(mobileNo: String): AdvisorDto {
        val advisor = advisorRepository.findByPrimaryMobileNo(mobileNo)
        if (advisor == null) {
            throw AdvisorMobileNotFoundException(mobileNo, messageSource)
        }
        return getAdvisorDTOWithPersonDetails(advisor)
    }

    private fun getAdvisorDTOWithPersonDetails(advisor: Advisor): AdvisorDto {
        val advisorDto = modelMapper.map(advisor, AdvisorDto::class.java)
        val personDetails = personReadService.getPerson(advisor.personId)
        advisorDto.personalDetails = personDetails
        return advisorDto
    }
}
