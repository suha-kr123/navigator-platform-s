package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "personCacheManager")
class PersonServiceImpl(
    private val personRepository: PersonRepository
) : PersonService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "person"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#personId")
    override fun getPerson(personId: UUID): PersonResponse {
        val person = personRepository.findById(personId)
            .orElseThrow { PersonNotFoundException(personId, messageSource) }
        return mapEntityToResponse(person)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobile_' + #mobileNo")
    override fun getPersonByMobileNo(mobileNo: String): PersonResponse {
        val person = personRepository.findByPrimaryMobileNo(mobileNo)
            ?: throw PersonNotFoundException(UUID.randomUUID(), messageSource) // We don't have the actual ID here
        return mapEntityToResponse(person)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createPerson(request: PersonCreateRequest): PersonResponse {
        val person = Person(
            firstName = request.firstName,
            middleName = request.middleName,
            lastName = request.lastName,
            mobileNumbers = request.mobileNumbers,
            email = request.email,
            dateOfBirth = request.dateOfBirth,
            gender = request.gender
        )

        val savedPerson = personRepository.save(person)
        return mapEntityToResponse(savedPerson)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#personId")
    override fun updatePerson(personId: UUID, request: PersonUpdateRequest): PersonResponse {
        val existingPerson = personRepository.findById(personId)
            .orElseThrow { PersonNotFoundException(personId, messageSource) }

        val updatedPerson = Person(
            id = existingPerson.id,
            firstName = request.firstName ?: existingPerson.firstName,
            middleName = request.middleName ?: existingPerson.middleName,
            lastName = request.lastName ?: existingPerson.lastName,
            mobileNumbers = if (request.mobileNumbers.isNotEmpty()) request.mobileNumbers else existingPerson.mobileNumbers,
            email = request.email ?: existingPerson.email,
            dateOfBirth = request.dateOfBirth ?: existingPerson.dateOfBirth,
            gender = request.gender ?: existingPerson.gender,
            dataExt = existingPerson.dataExt
        )

        val savedPerson = personRepository.save(updatedPerson)
        return mapEntityToResponse(savedPerson)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#personId")
    override fun deletePerson(personId: UUID) {
        val person = personRepository.findById(personId)
            .orElseThrow { PersonNotFoundException(personId, messageSource) }
        personRepository.deleteById(person.id!!)
    }

    private fun mapEntityToResponse(person: Person): PersonResponse {
        return PersonResponse(
            id = person.id,
            firstName = person.firstName,
            middleName = person.middleName,
            lastName = person.lastName,
            mobileNumbers = person.mobileNumbers,
            email = person.email,
            dateOfBirth = person.dateOfBirth,
            gender = person.gender
        )
    }
}
