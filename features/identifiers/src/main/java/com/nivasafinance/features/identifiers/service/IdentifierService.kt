package com.nivasafinance.features.identifiers.service

import com.nivasafinance.features.identifiers.dto.IdentifierRequest
import com.nivasafinance.features.identifiers.dto.IdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import java.util.UUID

interface IdentifierService {
    fun createIdentifier(identifierRequest: IdentifierRequest): IdentifierResponse
    fun updateIdentifier(identifierId: UUID, identifierUpdateRequest: IdentifierUpdateRequest): IdentifierResponse
    fun deleteIdentifier(identifierId: UUID)
    fun getIdentifierById(identifierId: UUID): IdentifierResponse
    fun getAllIdentifiers(): List<IdentifierResponse>
}
