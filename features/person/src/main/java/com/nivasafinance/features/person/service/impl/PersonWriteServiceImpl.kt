package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.enum.AddressType
import com.nivasafinance.features.address.exception.AddressTypeAlreadyExistsException
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.person.dto.EmploymentDetailsCreateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingUpdateRequest
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.dto.PersonIdentifierUpdateRequest
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.entity.EmploymentDetails
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.entity.PersonAddressMapping
import com.nivasafinance.features.person.entity.PersonIdentifier
import com.nivasafinance.features.person.enum.IdentifierType
import com.nivasafinance.features.person.exception.DuplicateIdentifierTypeException
import com.nivasafinance.features.person.exception.DuplicatePrimaryMobileNumberException
import com.nivasafinance.features.person.exception.EmploymentDetailsAlreadyExistsException
import com.nivasafinance.features.person.exception.EmploymentDetailsNotFoundException
import com.nivasafinance.features.person.exception.InvalidAddressTypeException
import com.nivasafinance.features.person.exception.InvalidIdentifierTypeException
import com.nivasafinance.features.person.exception.InvalidMobileNumberException
import com.nivasafinance.features.person.exception.PersonAddressMappingNotFoundException
import com.nivasafinance.features.person.exception.PersonIdentifierNotFoundException
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.exception.PrimaryMobileNumberAlreadyExistsException
import com.nivasafinance.features.person.repository.EmploymentDetailsRepository
import com.nivasafinance.features.person.repository.PersonAddressMappingRepository
import com.nivasafinance.features.person.repository.PersonIdentifierRepository
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonWriteService
import com.nivasafinance.features.person.util.MobileNumberValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PersonWriteServiceImpl(
    private val personRepository: PersonRepository,
    private val personAddressMappingRepository: PersonAddressMappingRepository,
    private val personIdentifierRepository: PersonIdentifierRepository,
    private val employmentDetailsRepository: EmploymentDetailsRepository,
    private val addressService: AddressService
) : PersonWriteService, BaseNavigatorService() {

    @Transactional
    override fun createPerson(request: PersonCreateRequest): UUID {
        request.mobileNumbers.forEach { mobileNumber ->
            if (!MobileNumberValidator.isValidIndianMobileNumber(mobileNumber.number)) {
                throw InvalidMobileNumberException(messageSource)
            }
        }

        val primaryNumbers = request.mobileNumbers.filter { it.isPrimary == true }
        if (primaryNumbers.size > 1) {
            throw DuplicatePrimaryMobileNumberException(messageSource)
        }
        primaryNumbers.forEach { primaryMobile ->
            primaryMobile.number?.let { mobileNumber ->
                val existingPerson = personRepository.findByPrimaryMobileNo(mobileNumber)
                if (existingPerson != null) {
                    throw PrimaryMobileNumberAlreadyExistsException(messageSource)
                }
            }
        }
        val cleanedMobileNumbers = request.mobileNumbers.map { mobileNumber ->
            mobileNumber.copy(number = MobileNumberValidator.cleanMobileNumber(mobileNumber.number))
        }

        val personEntity = Person(
            firstName = request.firstName,
            middleName = request.middleName,
            lastName = request.lastName,
            mobileNumbers = cleanedMobileNumbers,
            email = request.email,
            dateOfBirth = request.dateOfBirth,
            gender = request.gender
        )
        val savedPersonEntity = personRepository.save(personEntity)
        return savedPersonEntity.id!!
    }

    @Transactional
    override fun deletePerson(id: UUID) {
        if (!personRepository.existsById(id)) {
            throw PersonNotFoundException(id, messageSource)
        }
        personRepository.deleteById(id)
    }

    @Transactional
    override fun updatePerson(id: UUID, request: PersonUpdateRequest) {
        val existingPerson = personRepository.findById(id)
            .orElseThrow { PersonNotFoundException(id, messageSource) }

        // Update fields if provided
        request.firstName?.let { existingPerson.firstName = it }
        request.middleName?.let { existingPerson.middleName = it }
        request.lastName?.let { existingPerson.lastName = it }
        request.email?.let { existingPerson.email = it }
        request.dateOfBirth?.let { existingPerson.dateOfBirth = it }
        request.gender?.let { existingPerson.gender = it }

        // Update mobile numbers if provided
        if (request.mobileNumbers.isNotEmpty()) {
            // Validate all mobile numbers format
            request.mobileNumbers.forEach { mobileNumber ->
                if (!MobileNumberValidator.isValidIndianMobileNumber(mobileNumber.number)) {
                    throw InvalidMobileNumberException(messageSource)
                }
            }

            // Validate that there's only one primary mobile number in the request
            val primaryNumbers = request.mobileNumbers.filter { it.isPrimary == true }
            if (primaryNumbers.size > 1) {
                throw DuplicatePrimaryMobileNumberException(messageSource)
            }

            // Check if any of the primary mobile numbers already exist in the database (excluding current person)
            primaryNumbers.forEach { primaryMobile ->
                primaryMobile.number?.let { mobileNumber ->
                    val existingPersonWithMobile = personRepository.findByPrimaryMobileNo(mobileNumber)
                    if (existingPersonWithMobile != null && existingPersonWithMobile.id != id) {
                        throw PrimaryMobileNumberAlreadyExistsException(messageSource)
                    }
                }
            }

            // Clean and format mobile numbers
            val cleanedMobileNumbers = request.mobileNumbers.map { mobileNumber ->
                mobileNumber.copy(number = MobileNumberValidator.cleanMobileNumber(mobileNumber.number))
            }
            existingPerson.mobileNumbers = cleanedMobileNumbers
        }

        personRepository.save(existingPerson)
    }

    // Address mapping operations
    @Transactional
    override fun addAddressToPerson(
        personId: UUID,
        request: PersonAddressMappingRequest
    ): PersonAddressMappingResponse {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }

        // Validate address type
        val addressType = request.addressType
        if (addressType.isNullOrBlank()) {
            throw InvalidAddressTypeException.mandatory(messageSource)
        }

        if (!AddressType.isValid(addressType)) {
            throw InvalidAddressTypeException.withValidTypes(addressType, AddressType.getValidTypes(), messageSource)
        }

        // Check if person already has an address of this type
        if (personAddressMappingRepository.existsByPersonIdAndAddressType(personId, addressType)) {
            throw AddressTypeAlreadyExistsException(personId, addressType, messageSource)
        }

        // Create address from request details
        val addressCreateRequest = AddressCreateRequest(
            addressOne = request.addressOne,
            addressTwo = request.addressTwo,
            landmark = request.landmark,
            district = request.district,
            state = request.state,
            pincode = request.pincode,
            addressSource = request.addressSource ?: "CUSTOMER"
        )

        val addressResponse = addressService.createAddress(addressCreateRequest)
        val addressId = addressResponse.id!!

        val mapping = PersonAddressMapping(
            personId = personId,
            addressId = addressId,
            addressType = addressType
        )

        val savedMapping = personAddressMappingRepository.save(mapping)

        return PersonAddressMappingResponse(
            id = savedMapping.id!!,
            personId = personId,
            address = addressResponse,
            addressType = savedMapping.addressType
        )
    }

    @Transactional
    override fun updatePersonAddressMapping(
        personId: UUID,
        addressId: UUID,
        request: PersonAddressMappingUpdateRequest
    ): PersonAddressMappingResponse {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }

        val mapping = personAddressMappingRepository.findByPersonIdAndAddressId(personId, addressId)
            ?: throw PersonAddressMappingNotFoundException(personId, addressId, messageSource)

        // Validate address type if provided
        request.addressType?.let { addressType ->
            // Validate enum value
            if (!AddressType.isValid(addressType)) {
                throw InvalidAddressTypeException.withValidTypes(
                    addressType,
                    AddressType.getValidTypes(),
                    messageSource
                )
            }

            // Check if person already has an address of this type (excluding current address)
            val existingMappingWithType = personAddressMappingRepository.findByPersonId(personId)
                .find { it.addressType == addressType && it.addressId != addressId }

            if (existingMappingWithType != null) {
                throw AddressTypeAlreadyExistsException(personId, addressType, messageSource)
            }
        }

        // Update mapping fields if provided
        request.addressType?.let { mapping.addressType = it }
        val savedMapping = personAddressMappingRepository.save(mapping)

        // Update address details if pincode is provided (required field)
        val addressResponse = if (request.pincode != null) {
            val addressUpdateRequest = AddressUpdateRequest(
                addressOne = request.addressOne,
                addressTwo = request.addressTwo,
                landmark = request.landmark,
                district = request.district,
                state = request.state,
                pincode = request.pincode!!,
                addressSource = request.addressSource
            )
            addressService.updateAddress(addressId, addressUpdateRequest)
        } else {
            addressService.getAddress(addressId)
        }

        return PersonAddressMappingResponse(
            id = savedMapping.id!!,
            personId = personId,
            address = addressResponse,
            addressType = savedMapping.addressType
        )
    }

    @Transactional
    override fun removeAddressFromPerson(personId: UUID, addressId: UUID) {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }

        val mapping = personAddressMappingRepository.findByPersonIdAndAddressId(personId, addressId)
        if (mapping == null) {
            throw PersonAddressMappingNotFoundException(personId, addressId, messageSource)
        }

        // Delete the mapping first
        personAddressMappingRepository.delete(mapping)

        // Then delete the actual address
        addressService.deleteAddress(addressId)
    }

    // Identifier operations
    @Transactional
    override fun createPersonIdentifier(
        personId: UUID,
        request: PersonIdentifierCreateRequest
    ): PersonIdentifierResponse {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }

        // Validate identifier type
        if (!IdentifierType.isValid(request.type.name)) {
            throw InvalidIdentifierTypeException.withValidTypes(request.type.name, messageSource)
        }

        // Check if identifier type already exists for this person
        if (personIdentifierRepository.existsByPersonIdAndType(personId, request.type)) {
            throw DuplicateIdentifierTypeException(personId, request.type, messageSource)
        }

        val identifier = PersonIdentifier(
            personId = personId,
            identifier = request.identifier,
            type = request.type
        )

        val savedIdentifier = personIdentifierRepository.save(identifier)
        return mapIdentifierToResponse(savedIdentifier)
    }

    @Transactional
    override fun updatePersonIdentifier(id: UUID, request: PersonIdentifierUpdateRequest): PersonIdentifierResponse {
        val identifier = personIdentifierRepository.findById(id)
            .orElseThrow { PersonIdentifierNotFoundException(id, messageSource) }

        request.identifier?.let { identifier.identifier = it }
        request.type?.let { newType ->
            // Validate identifier type
            if (!IdentifierType.isValid(newType.name)) {
                throw InvalidIdentifierTypeException.withValidTypes(newType.name, messageSource)
            }

            // Check if the new type already exists for this person (excluding current identifier)
            val existingIdentifier = personIdentifierRepository.findByPersonIdAndType(identifier.personId, newType)
            if (existingIdentifier != null && existingIdentifier.id != id) {
                throw DuplicateIdentifierTypeException(identifier.personId, newType, messageSource)
            }
            identifier.type = newType
        }

        val savedIdentifier = personIdentifierRepository.save(identifier)
        return mapIdentifierToResponse(savedIdentifier)
    }

    @Transactional
    override fun deletePersonIdentifier(id: UUID) {
        if (!personIdentifierRepository.existsById(id)) {
            throw PersonIdentifierNotFoundException(id, messageSource)
        }
        personIdentifierRepository.deleteById(id)
    }

    // Employment details operations
    @Transactional
    override fun createPersonEmploymentDetails(
        personId: UUID,
        request: EmploymentDetailsCreateRequest
    ): EmploymentDetailsResponse {
        if (!personRepository.existsById(personId)) {
            throw PersonNotFoundException(personId, messageSource)
        }

        if (employmentDetailsRepository.existsByPersonId(personId)) {
            throw EmploymentDetailsAlreadyExistsException(personId, messageSource)
        }

        val employmentDetails = EmploymentDetails(
            personId = personId,
            employerName = request.employerName,
            employerType = request.employerType,
            jobTitle = request.jobTitle,
            department = request.department,
            employmentType = request.employmentType,
            location = request.location,
            salary = request.salary,
            documents = request.documents,
            extData = request.extData
        )

        val savedEmploymentDetails = employmentDetailsRepository.save(employmentDetails)
        return EmploymentDetailsResponse.fromEmploymentDetails(savedEmploymentDetails)
    }

    @Transactional
    override fun updatePersonEmploymentDetails(
        personId: UUID,
        request: EmploymentDetailsUpdateRequest
    ): EmploymentDetailsResponse {
        val employmentDetails = employmentDetailsRepository.findByPersonId(personId)
            ?: throw EmploymentDetailsNotFoundException(personId, messageSource)

        request.employerName?.let { employmentDetails.employerName = it }
        request.employerType?.let { employmentDetails.employerType = it }
        request.jobTitle?.let { employmentDetails.jobTitle = it }
        request.department?.let { employmentDetails.department = it }
        request.employmentType?.let { employmentDetails.employmentType = it }
        request.location?.let { employmentDetails.location = it }
        request.salary?.let { employmentDetails.salary = it }
        request.documents?.let { employmentDetails.documents = it }
        request.extData?.let { employmentDetails.extData = it }

        val updatedEmploymentDetails = employmentDetailsRepository.save(employmentDetails)
        return EmploymentDetailsResponse.fromEmploymentDetails(updatedEmploymentDetails)
    }

    @Transactional
    override fun deletePersonEmploymentDetails(personId: UUID) {
        val employmentDetails = employmentDetailsRepository.findByPersonId(personId)
            ?: throw EmploymentDetailsNotFoundException(personId, messageSource)
        employmentDetailsRepository.delete(employmentDetails)
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
