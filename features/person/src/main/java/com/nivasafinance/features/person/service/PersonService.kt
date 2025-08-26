package com.nivasafinance.features.person.service

import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingUpdateRequest
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.dto.PersonIdentifierUpdateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import java.util.UUID

interface PersonService {
    // Basic person operations
    fun getPerson(personId: UUID): PersonResponse
    fun createPerson(request: PersonCreateRequest): PersonResponse
    fun updatePerson(personId: UUID, request: PersonUpdateRequest): PersonResponse
    fun deletePerson(personId: UUID)

    // Address mapping operations
    fun getPersonAddresses(personId: UUID): List<PersonAddressMappingResponse>
    fun addAddressToPerson(personId: UUID, request: PersonAddressMappingRequest): PersonAddressMappingResponse
    fun updatePersonAddressMapping(
        personId: UUID,
        addressId: UUID,
        request: PersonAddressMappingUpdateRequest
    ): PersonAddressMappingResponse
    fun removeAddressFromPerson(personId: UUID, addressId: UUID)

    // Identifier operations
    fun getPersonIdentifiers(personId: UUID): List<PersonIdentifierResponse>
    fun createPersonIdentifier(personId: UUID, request: PersonIdentifierCreateRequest): PersonIdentifierResponse
    fun updatePersonIdentifier(
        personId: UUID,
        identifierId: UUID,
        request: PersonIdentifierUpdateRequest
    ): PersonIdentifierResponse
    fun deletePersonIdentifier(personId: UUID, identifierId: UUID)
}
