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

    @GetMapping("/{entityType}/{entityId}")
    fun getIdentifiersByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
    ): ResponseEntity<List<IdentifierResponse>> {
        val identifiers = identifierService.getIdentifiersByEntity(entityType, entityId)
        return ResponseEntity.ok(identifiers)
    }

    @PostMapping("/{entityType}/{entityId}")
    fun createIdentifier(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @RequestBody identifierRequest: IdentifierRequest
    ): ResponseEntity<IdentifierResponse> {
        val createdIdentifier = identifierService.createIdentifierByEntity(entityType, entityId, identifierRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIdentifier)
    }

    @PutMapping("/{entityType}/{entityId}/{identifierId}")
    fun updateIdentifier(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @PathVariable identifierId: UUID,
        @RequestBody identifierUpdateRequest: IdentifierUpdateRequest
    ): ResponseEntity<IdentifierResponse> {
        val updatedIdentifier = identifierService.updateIdentifierByEntity(entityType, entityId, identifierId, identifierUpdateRequest)
        return ResponseEntity.ok(updatedIdentifier)
    }

    @DeleteMapping("/{entityType}/{entityId}/{identifierId}")
    fun deleteIdentifier(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @PathVariable identifierId: UUID
    ): ResponseEntity<Void> {
        identifierService.deleteIdentifierByEntity(entityType, entityId, identifierId)
        return ResponseEntity.noContent().build()
    }
}
