package com.nivasafinance.features.person.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import java.util.UUID

interface PersonService {
    fun createPerson(personRequest: PersonCreateRequest): PersonResponse
    fun updatePerson(personId: UUID, personUpdateRequest: PersonUpdateRequest): PersonResponse
    fun getPerson(personId: UUID): PersonResponse
    fun getAllPersons(paginationRequest: PaginationRequest): PaginatedResponse<PersonResponse>
}
