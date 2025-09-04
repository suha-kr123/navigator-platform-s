package com.nivasafinance.features.person.service

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
import java.util.UUID

interface PersonWriteService {

    fun createPerson(request: PersonCreateRequest): UUID
    fun deletePerson(id: UUID)
    fun updatePerson(id: UUID, request: PersonUpdateRequest)

    // Address mapping operations
    fun addAddressToPerson(personId: UUID, request: PersonAddressMappingRequest): PersonAddressMappingResponse
    fun updatePersonAddressMapping(
        personId: UUID,
        addressId: UUID,
        request: PersonAddressMappingUpdateRequest
    ): PersonAddressMappingResponse
    fun removeAddressFromPerson(personId: UUID, addressId: UUID)

    // Identifier operations
    fun createPersonIdentifier(personId: UUID, request: PersonIdentifierCreateRequest): PersonIdentifierResponse
    fun updatePersonIdentifier(id: UUID, request: PersonIdentifierUpdateRequest): PersonIdentifierResponse
    fun deletePersonIdentifier(id: UUID)

    // Employment details operations
    fun createPersonEmploymentDetails(
        personId: UUID,
        request: EmploymentDetailsCreateRequest
    ): EmploymentDetailsResponse
    fun updatePersonEmploymentDetails(
        personId: UUID,
        request: EmploymentDetailsUpdateRequest
    ): EmploymentDetailsResponse
    fun deletePersonEmploymentDetails(personId: UUID)
}
