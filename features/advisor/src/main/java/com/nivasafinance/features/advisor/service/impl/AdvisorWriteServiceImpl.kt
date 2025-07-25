package com.nivasafinance.features.advisor.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorMobileAlreadyExistsException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.advisor.service.AdvisorWriteService
import com.nivasafinance.features.person.service.PersonWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorWriteServiceImpl(
    private val advisorRepository: AdvisorRepository,
    private val personWriteService: PersonWriteService,
    private val advisorReadService: AdvisorReadService,
) : AdvisorWriteService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor"
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createAdvisor(advisorDto: AdvisorDto): AdvisorDto {
        val mobileNo = advisorDto.personalDetails.mobileNumber.primary
        val existingAdvisor = try {
            advisorReadService.getAdvisorByMobileNo(mobileNo)
        } catch (_: Exception) {
            null
        }
        if (existingAdvisor != null) {
            throw AdvisorMobileAlreadyExistsException(mobileNo, messageSource)
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
        personWriteService.deletePerson(personId)
        advisorRepository.deleteById(id)
        return advisor
    }

    @Caching(
        put = [
            CachePut(cacheNames = [CACHE_NAME], key = "#id"),
            CachePut(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #result.personalDetails.mobileNumber.primary")
        ]
    )
    @Transactional
    override fun updateAdvisor(id: UUID, advisorDto: AdvisorDto): AdvisorDto {
        val advisor = advisorRepository.findById(id).orElseThrow { AdvisorNotFoundException(id, messageSource) }
        val personId = advisor.personId
        val savedPersonDto = personWriteService.updatePerson(personId, advisorDto.personalDetails)
        val savedAdvisor = advisorRepository.save(advisor)
        val savedAdvisorDto = modelMapper.map(savedAdvisor, AdvisorDto::class.java)
        savedAdvisorDto.personalDetails = savedPersonDto
        return savedAdvisorDto
    }
}
