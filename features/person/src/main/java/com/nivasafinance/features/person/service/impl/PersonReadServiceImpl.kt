package com.nivasafinance.features.person.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PersonReadServiceImpl(
    private val personRepository: PersonRepository,
) : PersonReadService, BaseNavigatorService() {
    companion object {
        private const val CACHE_NAME = "person"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getPerson(id: UUID): PersonDto {
        val person = personRepository.findById(id).orElseThrow {
            // Use the new PersonNotFoundException with a localized message
            PersonNotFoundException(id, messageSource)
        }

        return modelMapper.map(person, PersonDto::class.java)
    }
}
