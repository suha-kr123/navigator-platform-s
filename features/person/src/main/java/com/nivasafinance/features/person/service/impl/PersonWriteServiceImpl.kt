package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.exception.AddressTypeAlreadyExistsException
import com.nivasafinance.features.address.exception.AddressTypeNotFoundException
import com.nivasafinance.features.address.service.AddressWriteService
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.AddressDetails
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import com.nivasafinance.features.person.service.PersonWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PersonWriteServiceImpl(
    private val personRepository: PersonRepository,
    private val personReadService: PersonReadService,
    private val addressWriteService: AddressWriteService,
) : PersonWriteService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "person"
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#result.id")
    override fun savePerson(personDto: PersonDto): PersonDto {
        val personEntity = modelMapper.map(personDto, Person::class.java)
        val savedPersonEntity = personRepository.save(personEntity)
        val savedDto = modelMapper.map(savedPersonEntity, PersonDto::class.java)
        return savedDto
    }

    @CacheEvict(cacheNames = [CACHE_NAME], key = "#id")
    @Transactional
    override fun deletePerson(id: UUID) {
        if (!personRepository.existsById(id)) {
            throw PersonNotFoundException(id, messageSource)
        }
        personRepository.deleteById(id)
    }

    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    @Transactional
    override fun updatePerson(id: UUID, personDto: PersonDto): PersonDto {
        val existingPerson = personRepository.findById(id)
            .orElseThrow { PersonNotFoundException(id, messageSource) }
        modelMapper.map(personDto, existingPerson)
        val savedPerson = personRepository.save(existingPerson)
        return modelMapper.map(savedPerson, PersonDto::class.java)
    }

    @Transactional
    @CachePut(cacheNames = [CACHE_NAME], key = "#personId")
    override fun addAddress(personId: UUID, addressDTO: AddressCreateRequest, addressType: String): AddressResponse {
        // Verify person exists using PersonReadService
        personReadService.getPerson(personId)

        // Get person entity from repository for modification
        val person = personRepository.findById(personId)
            .orElseThrow { PersonNotFoundException(personId, messageSource) }

        val existingAddresses = person.addresses?.toMutableList() ?: mutableListOf()
        if (existingAddresses.any { it.type == addressType }) {
            throw AddressTypeAlreadyExistsException(personId, addressType, messageSource)
        }

        // Create the address using address service
        val addressResponse = addressWriteService.createAddress(addressDTO)

        val newAddressDetail = AddressDetails(
            addressId = addressResponse.id,
            type = addressType
        )

        existingAddresses.add(newAddressDetail)
        person.addresses = existingAddresses
        personRepository.save(person)

        return addressResponse
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#personId")
    override fun removeAddress(personId: UUID, addressId: UUID) {
        val person = personRepository.findById(personId)
            .orElseThrow { PersonNotFoundException(personId, messageSource) }

        val existingAddresses = person.addresses?.toMutableList() ?: mutableListOf()
        val addressToRemove = existingAddresses.find { it.addressId == addressId }
            ?: throw AddressNotFoundException(addressId, messageSource)

        existingAddresses.remove(addressToRemove)
        person.addresses = existingAddresses
        personRepository.save(person)

        addressWriteService.deleteAddress(addressId)
    }

    @Transactional
    @CacheEvict(cacheNames = [CACHE_NAME], key = "#personId")
    override fun removeAddress(personId: UUID, addressType: String) {
        val person = personRepository.findById(personId)
            .orElseThrow { PersonNotFoundException(personId, messageSource) }

        val existingAddresses = person.addresses?.toMutableList() ?: mutableListOf()
        val addressToRemove = existingAddresses.find { it.type == addressType }
            ?: throw AddressTypeNotFoundException(personId, addressType, messageSource)

        existingAddresses.remove(addressToRemove)
        person.addresses = existingAddresses
        personRepository.save(person)

        addressToRemove.addressId?.let { id ->
            addressWriteService.deleteAddress(id)
        }
    }
}
