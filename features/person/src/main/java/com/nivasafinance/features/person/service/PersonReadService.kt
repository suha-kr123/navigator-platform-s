package com.nivasafinance.features.person.service

import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import java.util.UUID

interface PersonReadService {

    // Basic person operations
    fun getPerson(id: UUID): PersonData

    // Address mapping operations
    fun getPersonAddresses(personId: UUID): List<PersonAddressMappingResponse>

    // Identifier operations
    fun getPersonIdentifiers(personId: UUID): List<PersonIdentifierResponse>
}
