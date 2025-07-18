package com.nivasafinance.features.person.service

import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PersonService(@Autowired private val personRepository: PersonRepository) {

    @Cacheable(cacheNames = ["person"], key = "#id")
    fun getPerson(id: UUID): Person {
        return personRepository.findById(id).orElse(null)
    }
}
