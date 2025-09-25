package com.nivasafinance.features.identifiers.controller

import com.nivasafinance.features.identifiers.dto.IdentifierRequest
import com.nivasafinance.features.identifiers.dto.IdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import com.nivasafinance.features.identifiers.service.IdentifierService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/identifiers")
class IdentifierController(private val identifierService: IdentifierService) {

    @GetMapping
    fun getAllIdentifiers(): ResponseEntity<List<IdentifierResponse>> {
        val identifiers = identifierService.getAllIdentifiers()
        return ResponseEntity.ok(identifiers)
    }

    @GetMapping("/{identifierId}")
    fun getIdentifierById(@PathVariable identifierId: UUID): ResponseEntity<IdentifierResponse> {
        val identifier = identifierService.getIdentifierById(identifierId)
        return ResponseEntity.ok(identifier)
    }

    @PostMapping
    fun createIdentifier(@RequestBody identifierRequest: IdentifierRequest): ResponseEntity<IdentifierResponse> {
        val createdIdentifier = identifierService.createIdentifier(identifierRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIdentifier)
    }

    @PutMapping("/{identifierId}")
    fun updateIdentifier(
        @PathVariable identifierId: UUID,
        @RequestBody identifierUpdateRequest: IdentifierUpdateRequest
    ): ResponseEntity<IdentifierResponse> {
        val updatedIdentifier = identifierService.updateIdentifier(identifierId, identifierUpdateRequest)
        return ResponseEntity.ok(updatedIdentifier)
    }

    @DeleteMapping("/{identifierId}")
    fun deleteIdentifier(@PathVariable identifierId: UUID): ResponseEntity<Void> {
        identifierService.deleteIdentifier(identifierId)
        return ResponseEntity.noContent().build()
    }
}
