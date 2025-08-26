package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingUpdateRequest
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
@RequestMapping("/v1/persons/{personId}/addresses")
class PersonAddressController(
    private val personService: PersonService
) {

    @PostMapping
    fun addAddressToPerson(
        @PathVariable personId: UUID,
        @RequestBody @Valid request: PersonAddressMappingRequest
    ): ResponseEntity<PersonAddressMappingResponse> {
        val mapping = personService.addAddressToPerson(personId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping)
    }

    @GetMapping
    fun getPersonAddresses(@PathVariable personId: UUID): ResponseEntity<List<PersonAddressMappingResponse>> {
        val addresses = personService.getPersonAddresses(personId)
        return ResponseEntity.ok(addresses)
    }

    @PatchMapping("/{addressId}")
    fun updatePersonAddressMapping(
        @PathVariable personId: UUID,
        @PathVariable addressId: UUID,
        @RequestBody @Valid request: PersonAddressMappingUpdateRequest
    ): ResponseEntity<PersonAddressMappingResponse> {
        val mapping = personService.updatePersonAddressMapping(personId, addressId, request)
        return ResponseEntity.ok(mapping)
    }

    @DeleteMapping("/{addressId}")
    fun removeAddressFromPerson(
        @PathVariable personId: UUID,
        @PathVariable addressId: UUID
    ): ResponseEntity<Unit> {
        personService.removeAddressFromPerson(personId, addressId)
        return ResponseEntity.noContent().build()
    }
}
