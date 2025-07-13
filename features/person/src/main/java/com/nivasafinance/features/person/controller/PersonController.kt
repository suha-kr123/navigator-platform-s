package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class PersonController(@Autowired private val personRepository: PersonRepository) {

    @GetMapping("/persons")
    fun creditCheck(): List<Person> {
        return personRepository.findAll()
    }
}