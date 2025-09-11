package com.nivasafinance.features.person.service

import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import java.util.UUID

interface PersonService {
    // Basic person operations
    fun getPerson(personId: UUID): PersonResponse
    fun getPersonByMobileNo(mobileNo: String): PersonResponse
    fun createPerson(request: PersonCreateRequest): PersonResponse
    fun updatePerson(personId: UUID, request: PersonUpdateRequest): PersonResponse
    fun deletePerson(personId: UUID)
}
