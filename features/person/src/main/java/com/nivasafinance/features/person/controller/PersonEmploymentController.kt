package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.dto.EmploymentDetailsCreateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.service.PersonService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/persons/{personId}/employment")
class PersonEmploymentController(
    private val personService: PersonService
) {

    @PostMapping
    fun createEmploymentDetails(
        @PathVariable personId: UUID,
        @RequestBody @Valid request: EmploymentDetailsCreateRequest
    ): ResponseEntity<EmploymentDetailsResponse> {
        val employmentDetails = personService.createPersonEmploymentDetails(personId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(employmentDetails)
    }

    @GetMapping
    fun getEmploymentDetails(@PathVariable personId: UUID): ResponseEntity<EmploymentDetailsResponse?> {
        val employmentDetails = personService.getPersonEmploymentDetails(personId)
        return ResponseEntity.ok(employmentDetails)
    }

    @PatchMapping
    fun updateEmploymentDetails(
        @PathVariable personId: UUID,
        @RequestBody @Valid request: EmploymentDetailsUpdateRequest
    ): ResponseEntity<EmploymentDetailsResponse> {
        val employmentDetails = personService.updatePersonEmploymentDetails(personId, request)
        return ResponseEntity.ok(employmentDetails)
    }

    @DeleteMapping
    fun deleteEmploymentDetails(@PathVariable personId: UUID): ResponseEntity<Unit> {
        personService.deletePersonEmploymentDetails(personId)
        return ResponseEntity.noContent().build()
    }
}
