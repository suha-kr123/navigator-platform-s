package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PersonController(
    @Autowired private val personService: PersonService,
    @Autowired private val personRepository: PersonRepository
) {

    @GetMapping("/persons")
    fun creditCheck(): List<Person> {
        return personRepository.findAll()
    }

    @GetMapping("/persons/{personId}")
    fun creditCheck(@PathVariable personId: Long): Person {
        return personService.getPerson(personId)
    }
}
