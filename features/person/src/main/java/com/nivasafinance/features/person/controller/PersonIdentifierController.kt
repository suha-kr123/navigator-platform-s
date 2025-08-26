package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.dto.PersonIdentifierUpdateRequest
import com.nivasafinance.features.person.service.PersonService
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
@RequestMapping("/v1/persons/{personId}/identifiers")
class PersonIdentifierController(
    private val personService: PersonService
) {

    @GetMapping
    fun getPersonIdentifiers(@PathVariable personId: UUID): ResponseEntity<List<PersonIdentifierResponse>> {
        val identifiers = personService.getPersonIdentifiers(personId)
        return ResponseEntity.ok(identifiers)
    }

    @PostMapping
    fun createPersonIdentifier(
        @PathVariable personId: UUID,
        @RequestBody @Valid request: PersonIdentifierCreateRequest
    ): ResponseEntity<PersonIdentifierResponse> {
        val identifier = personService.createPersonIdentifier(personId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(identifier)
    }

    @PutMapping("/{identifierId}")
    fun updatePersonIdentifier(
        @PathVariable personId: UUID,
        @PathVariable identifierId: UUID,
        @RequestBody @Valid request: PersonIdentifierUpdateRequest
    ): ResponseEntity<PersonIdentifierResponse> {
        val identifier = personService.updatePersonIdentifier(personId, identifierId, request)
        return ResponseEntity.ok(identifier)
    }

    @DeleteMapping("/{identifierId}")
    fun deletePersonIdentifier(@PathVariable personId: UUID, @PathVariable identifierId: UUID): ResponseEntity<Unit> {
        personService.deletePersonIdentifier(personId, identifierId)
        return ResponseEntity.noContent().build()
    }
}
