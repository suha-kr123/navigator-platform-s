package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.advisor.service.AdvisorWriteService
import com.nivasafinance.features.person.service.PersonWriteService
import org.modelmapper.ModelMapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Caching
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorWriteServiceImpl(
    private val advisorRepository: AdvisorRepository,
    private val personWriteService: PersonWriteService,
    private val advisorReadService: AdvisorReadService,
    private val modelMapper: ModelMapper,
    private val messageSource: MessageSource
) : AdvisorWriteService {


    companion object {
        private const val CACHE_NAME = "advisor"
        private val locale = LocaleContextHolder.getLocale()
        private const val ADVISOR_ID_NOT_FOUND_KEY = "error.advisor.id.not.found"
        private const val ADVISOR_MOBILE_REQUIRED_KEY = "error.advisor.mobile.required"
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createAdvisor(advisorDto: AdvisorDto): AdvisorDto {

        val existingAdvisor = try {
            advisorReadService.getAdvisorByMobileNo(advisorDto.personalDetails.mobileNumber.primary)
        } catch (_: Exception) {
            null
        }
        if (existingAdvisor != null) {
            throw RuntimeException("Advisor with mobile number ${advisorDto.personalDetails.mobileNumber.primary} already exists.")
        }
        val savedPerson = personWriteService.savePerson(advisorDto.personalDetails)
        val savePersonId = requireNotNull(savedPerson.id) {
            "id should not be null after saving person: $savedPerson"
        }
        val advisor = Advisor(
            id = UUID.randomUUID(),
            personId = savePersonId,
            advisorCode = UUID.randomUUID().toString(),
            status = AdvisorStatus.CREATED
        )

        val savedAdvisor = advisorRepository.save(advisor)
        val savedAdvisorDto = modelMapper.map(savedAdvisor, AdvisorDto::class.java)
        savedAdvisorDto.personalDetails = savedPerson
        return savedAdvisorDto

    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#id"),
            CacheEvict(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #result.personalDetails.mobileNumber.primary")
        ]
    )
    override fun deleteAdvisor(id: UUID): AdvisorDto {
        val advisor = advisorReadService.getAdvisor(id)
        val personId = requireNotNull(advisor.personalDetails.id) {
            "personalDetails.id should not be null for advisor: $advisor"
        }
        personWriteService.deletePerson(personId);
        advisorRepository.deleteById(id)
        return advisor
    }

    @Caching(
        put = [
            CachePut(cacheNames = [CACHE_NAME], key = "#advisorDto.id"),
            CachePut(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #result.personalDetails.mobileNumber.primary")
        ]
    )
    override fun updateAdvisor(id: UUID, advisorDto: AdvisorDto): AdvisorDto {
        if (!advisorRepository.existsById(id)) {
            throw RuntimeException(messageSource.getMessage(ADVISOR_ID_NOT_FOUND_KEY, arrayOf(id), locale))
        }
        val advisor = modelMapper.map(advisorDto, Advisor::class.java)
        val savedPersonDto = personWriteService.savePerson(advisorDto.personalDetails)
        val savedAdvisor = advisorRepository.save(advisor)
        val savedAdvisorDto = modelMapper.map(savedAdvisor, AdvisorDto::class.java)
        savedAdvisorDto.personalDetails = savedPersonDto
        return savedAdvisorDto
    }
}