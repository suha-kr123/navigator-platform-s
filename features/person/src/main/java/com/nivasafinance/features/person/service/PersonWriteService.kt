package com.nivasafinance.features.person.service

import com.nivasafinance.features.person.dto.PersonDto
import java.util.UUID

interface PersonWriteService {

    fun savePerson(personDto: PersonDto): PersonDto
    fun deletePerson(id: UUID)
    fun updatePerson(id : UUID, personDto: PersonDto) : PersonDto
}
