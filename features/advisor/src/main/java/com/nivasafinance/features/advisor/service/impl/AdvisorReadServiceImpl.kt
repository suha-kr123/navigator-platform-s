package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.person.service.PersonReadService
import org.modelmapper.ModelMapper
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorReadServiceImpl(
    private val personReadService: PersonReadService,
    private val advisorRepository: AdvisorRepository,
    private val modelMapper: ModelMapper,
    private val messageSource: MessageSource
) : AdvisorReadService {


    companion object {
        private const val CACHE_NAME = "advisor"
        private val locale = LocaleContextHolder.getLocale()
        private const val ADVISOR_ID_NOT_FOUND_KEY = "error.advisor.id.not.found"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAdvisor(id: UUID): AdvisorDto {
        val advisor = advisorRepository.findById(id).orElseThrow {
            RuntimeException(messageSource.getMessage(ADVISOR_ID_NOT_FOUND_KEY, arrayOf(id), locale))
        }
        return getAdvisorDTOWithPersonDetails(advisor)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #mobileNo")
    override fun getAdvisorByMobileNo(mobileNo: String): AdvisorDto {
        val advisor = advisorRepository.findByPrimaryMobileNo(mobileNo)
        if (advisor == null) {
            throw RuntimeException("Advisor with mobile number $mobileNo not found.")
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