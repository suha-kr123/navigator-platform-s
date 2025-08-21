package com.nivasafinance.features.person.service

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.person.dto.PersonDto
import java.util.UUID

interface PersonWriteService {

    fun savePerson(personDto: PersonDto): PersonDto
    fun deletePerson(id: UUID)
    fun updatePerson(id: UUID, personDto: PersonDto): PersonDto

    // Address management methods
    fun addAddress(personId: UUID, addressDTO: AddressCreateRequest, addressType: String): AddressResponse
    fun removeAddress(personId: UUID, addressId: UUID)
    fun removeAddress(personId: UUID, addressType: String)
}
