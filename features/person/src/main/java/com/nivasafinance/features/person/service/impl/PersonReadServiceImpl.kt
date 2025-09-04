package com.nivasafinance.features.person.service.impl
import base.BaseNavigatorService
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.entity.PersonIdentifier
import com.nivasafinance.features.person.exception.PersonMobileNotFoundException
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.EmploymentDetailsRepository
import com.nivasafinance.features.person.repository.PersonAddressMappingRepository
import com.nivasafinance.features.person.repository.PersonIdentifierRepository
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
@Service
class PersonReadServiceImpl(
    private val personRepository: PersonRepository,
    private val personAddressMappingRepository: PersonAddressMappingRepository,
    private val personIdentifierRepository: PersonIdentifierRepository,
    private val employmentDetailsRepository: EmploymentDetailsRepository,
    private val addressService: AddressService
) : PersonReadService, BaseNavigatorService() {
    companion object {
        private val logger = LoggerFactory.getLogger(PersonReadServiceImpl::class.java)
    }
    override fun getPerson(id: UUID): PersonData {
        val person = personRepository.findById(id).orElseThrow {
            PersonNotFoundException(id, messageSource)
        }
        return PersonData.fromEntity(person)
    }

    override fun getPersonByMobileNo(mobileNo: String): PersonData {
        val person = personRepository.findByPrimaryMobileNo(mobileNo)
        if (person == null) {
            throw PersonMobileNotFoundException(mobileNo, messageSource)
        }
        return PersonData.fromEntity(person)
    }

    override fun getPersonAddresses(personId: UUID): List<PersonAddressMappingResponse> {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }
        val mappings = personAddressMappingRepository.findByPersonId(personId)
        return mappings.mapNotNull { mapping ->
            mapping.addressId?.let { addressId ->
                try {
                    val addressResponse = addressService.getAddress(addressId)
                    PersonAddressMappingResponse(
                        id = mapping.id!!,
                        personId = personId,
                        address = addressResponse,
                        addressType = mapping.addressType
                    )
                } catch (e: AddressNotFoundException) {
                    // Log the exception for debugging but don't fail the entire operation
                    logger.warn("Failed to fetch address $addressId for person $personId: ${e.message}")
                    null
                }
            }
        }
    }
    override fun getPersonIdentifiers(personId: UUID): List<PersonIdentifierResponse> {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }
        val identifiers = personIdentifierRepository.findByPersonId(personId)
        return identifiers.map { mapIdentifierToResponse(it) }
    }

    override fun getPersonEmploymentDetails(personId: UUID): EmploymentDetailsResponse? {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }
        val employmentDetails = employmentDetailsRepository.findByPersonId(personId)
        return employmentDetails?.let { EmploymentDetailsResponse.fromEmploymentDetails(it) }
    }

    private fun mapIdentifierToResponse(identifier: PersonIdentifier): PersonIdentifierResponse {
        return PersonIdentifierResponse(
            id = identifier.id!!,
            personId = identifier.personId,
            identifier = identifier.identifier,
            type = identifier.type
        )
    }
}
