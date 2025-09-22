package com.nivasafinance.features.identifiers.service

import com.nivasafinance.features.identifiers.dto.IdentifierRequest
import com.nivasafinance.features.identifiers.dto.IdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import java.util.UUID

interface IdentifierService {
    fun createIdentifierByEntity(entityType: String, entityId: UUID, identifierRequest: IdentifierRequest): IdentifierResponse
    fun updateIdentifierByEntity(entityType: String, entityId: UUID, identifierId: UUID, identifierUpdateRequest: IdentifierUpdateRequest): IdentifierResponse
    fun deleteIdentifierByEntity(entityType: String, entityId: UUID, identifierId: UUID)
    fun getIdentifiersByEntity(entityType: String, entityId: UUID): List<IdentifierResponse>
}
