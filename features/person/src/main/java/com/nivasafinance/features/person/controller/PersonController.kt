package com.nivasafinance.features.person.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import base.model.SortDirection
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.service.PersonService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/persons")
class PersonController(
    private val personService: PersonService
) {

    @PostMapping
    fun createPerson(
        @RequestBody personRequest: PersonCreateRequest
    ): ResponseEntity<PersonResponse> {
        val personResponse = personService.createPerson(personRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(personResponse)
    }

    @GetMapping("/{personId}")
    fun getPerson(@PathVariable personId: UUID): ResponseEntity<PersonResponse> {
        val personResponse = personService.getPerson(personId)
        return ResponseEntity.ok(personResponse)
    }

    @GetMapping
    fun getAllPersons(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<PersonResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = if (sortDirection == "ASC") SortDirection.ASC else SortDirection.DESC
        )
        val personsResponse = personService.getAllPersons(paginationRequest)
        return ResponseEntity.ok(personsResponse)
    }

    @PutMapping("/{personId}")
    fun updatePerson(
        @PathVariable personId: UUID,
        @RequestBody personUpdateRequest: PersonUpdateRequest
    ): ResponseEntity<PersonResponse> {
        val personResponse = personService.updatePerson(personId, personUpdateRequest)
        return ResponseEntity.ok(personResponse)
    }
}
