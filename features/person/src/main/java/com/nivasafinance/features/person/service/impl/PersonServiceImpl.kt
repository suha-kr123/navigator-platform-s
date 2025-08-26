package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingUpdateRequest
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.dto.PersonIdentifierUpdateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import com.nivasafinance.features.person.service.PersonService
import com.nivasafinance.features.person.service.PersonWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PersonServiceImpl(
    private val personReadService: PersonReadService,
    private val personWriteService: PersonWriteService,
    private val personRepository: PersonRepository
) : PersonService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "person"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#personId")
    override fun getPerson(personId: UUID): PersonResponse {
        val personData = personReadService.getPerson(personId)
        return mapDataToResponse(personData)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun createPerson(request: PersonCreateRequest): PersonResponse {
        val personId = personWriteService.createPerson(request)
        val person = personRepository.findById(personId).orElseThrow {
            PersonNotFoundException(personId, messageSource)
        }
        val personData = PersonData.fromEntity(person)
        return mapDataToResponse(personData)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#personId")
    override fun updatePerson(personId: UUID, request: PersonUpdateRequest): PersonResponse {
        personWriteService.updatePerson(personId, request)
        val personData = personReadService.getPerson(personId)
        return mapDataToResponse(personData)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#personId")
    override fun deletePerson(personId: UUID) {
        personWriteService.deletePerson(personId)
    }

    // Address mapping operations
    @Transactional
    @CacheEvict(cacheNames = ["person_addresses"], key = "#personId")
    override fun addAddressToPerson(
        personId: UUID,
        request: PersonAddressMappingRequest
    ): PersonAddressMappingResponse {
        return personWriteService.addAddressToPerson(personId, request)
    }

    @Cacheable(cacheNames = ["person_addresses"], key = "#personId")
    override fun getPersonAddresses(personId: UUID): List<PersonAddressMappingResponse> {
        return personReadService.getPersonAddresses(personId)
    }

    @Transactional
    @CacheEvict(cacheNames = ["person_addresses"], key = "#personId")
    override fun updatePersonAddressMapping(
        personId: UUID,
        addressId: UUID,
        request: PersonAddressMappingUpdateRequest
    ): PersonAddressMappingResponse {
        return personWriteService.updatePersonAddressMapping(personId, addressId, request)
    }

    @Transactional
    @CacheEvict(cacheNames = ["person_addresses"], key = "#personId")
    override fun removeAddressFromPerson(personId: UUID, addressId: UUID) {
        personWriteService.removeAddressFromPerson(personId, addressId)
    }

    // Identifier operations
    @Cacheable(cacheNames = ["person_identifiers"], key = "#personId")
    override fun getPersonIdentifiers(personId: UUID): List<PersonIdentifierResponse> {
        return personReadService.getPersonIdentifiers(personId)
    }

    @Transactional
    @CacheEvict(cacheNames = ["person_identifiers"], key = "#personId")
    override fun createPersonIdentifier(
        personId: UUID,
        request: PersonIdentifierCreateRequest
    ): PersonIdentifierResponse {
        return personWriteService.createPersonIdentifier(personId, request)
    }

    @Transactional
    @CacheEvict(cacheNames = ["person_identifiers"], key = "#personId")
    override fun updatePersonIdentifier(
        personId: UUID,
        identifierId: UUID,
        request: PersonIdentifierUpdateRequest
    ): PersonIdentifierResponse {
        return personWriteService.updatePersonIdentifier(identifierId, request)
    }

    @Transactional
    @CacheEvict(cacheNames = ["person_identifiers"], key = "#personId")
    override fun deletePersonIdentifier(personId: UUID, identifierId: UUID) {
        personWriteService.deletePersonIdentifier(identifierId)
    }

    private fun mapDataToResponse(personData: PersonData): PersonResponse {
        return PersonResponse(
            id = personData.id,
            firstName = personData.firstName,
            middleName = personData.middleName,
            lastName = personData.lastName,
            mobileNumbers = personData.mobileNumbers,
            email = personData.email,
            dateOfBirth = personData.dateOfBirth,
            gender = personData.gender
        )
    }
}
