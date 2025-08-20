package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.service.PersonReadService
import com.nivasafinance.features.person.service.PersonWriteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("v1/person")
class PersonController(
    private val personReadService: PersonReadService,
    private val personWriteService: PersonWriteService
) {

    @PostMapping
    fun createPerson(@RequestBody @Valid personDto: PersonDto): ResponseEntity<PersonDto> {
        val savedPersonDto = personWriteService.savePerson(personDto)
        return ResponseEntity(savedPersonDto, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getPerson(@PathVariable id: UUID): ResponseEntity<PersonDto> {
        val person = personReadService.getPerson(id)
        return ResponseEntity.ok(person)
    }

    @PutMapping("/{id}")
    fun updatePerson(@PathVariable id: UUID, @RequestBody personDto: PersonDto): ResponseEntity<PersonDto> {
        val updatedPerson = personWriteService.updatePerson(id, personDto)
        return ResponseEntity.ok(updatedPerson)
    }

    @DeleteMapping("/{id}")
    fun deletePerson(@PathVariable id: UUID): ResponseEntity<Unit> {
        personWriteService.deletePerson(id)
        return ResponseEntity.noContent().build()
    }
}
