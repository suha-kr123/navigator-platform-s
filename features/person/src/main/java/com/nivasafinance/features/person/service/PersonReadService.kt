package com.nivasafinance.features.person.service

import com.nivasafinance.features.person.dto.PersonDto
import java.util.UUID

interface PersonReadService {

    fun getPerson(id: UUID): PersonDto
}
