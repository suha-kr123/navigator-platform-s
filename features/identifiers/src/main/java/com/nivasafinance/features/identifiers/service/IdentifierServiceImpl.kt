package com.nivasafinance.features.identifiers.service

import com.nivasafinance.features.identifiers.dto.IdentifierRequest
import com.nivasafinance.features.identifiers.dto.IdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import com.nivasafinance.features.identifiers.entity.Identifier
import com.nivasafinance.features.identifiers.repository.IdentifierRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class IdentifierServiceImpl(
    private val identifierRepositoryWrapper: IdentifierRepositoryWrapper,
    private val messageSource: MessageSource
) : IdentifierService {

    override fun createIdentifier(identifierRequest: IdentifierRequest): IdentifierResponse {
        val identifier = Identifier(
            identifier = identifierRequest.identifier,
            type = identifierRequest.type,
            verificationStatus = identifierRequest.verificationStatus,
            verificationNotes = identifierRequest.verificationNotes,
            extData = identifierRequest.extData
        )
        val savedIdentifier = identifierRepositoryWrapper.saveWithException(identifier)
        return toIdentifierResponse(savedIdentifier)
    }

    override fun updateIdentifier(identifierId: UUID, identifierUpdateRequest: IdentifierUpdateRequest): IdentifierResponse {
        val existingIdentifier = identifierRepositoryWrapper.findByIdWithException(identifierId)

        identifierUpdateRequest.identifier?.let {
            existingIdentifier.identifier = it
        }
        identifierUpdateRequest.type?.let { existingIdentifier.type = it }
        identifierUpdateRequest.verificationStatus?.let { existingIdentifier.verificationStatus = it }
        identifierUpdateRequest.verificationNotes?.let { existingIdentifier.verificationNotes = it }
        identifierUpdateRequest.extData?.let { existingIdentifier.extData = it }

        val savedIdentifier = identifierRepositoryWrapper.saveWithException(existingIdentifier)
        return toIdentifierResponse(savedIdentifier)
    }

    override fun deleteIdentifier(identifierId: UUID) {
        identifierRepositoryWrapper.findByIdWithException(identifierId) // Check if exists
        identifierRepositoryWrapper.deleteByIdWithException(identifierId)
    }

    override fun getIdentifierById(identifierId: UUID): IdentifierResponse {
        val identifier = identifierRepositoryWrapper.findByIdWithException(identifierId)
        return toIdentifierResponse(identifier)
    }

    override fun getAllIdentifiers(): List<IdentifierResponse> {
        val identifiers = identifierRepositoryWrapper.findAllWithException()
        return identifiers.map { toIdentifierResponse(it) }
    }

    private fun toIdentifierResponse(identifier: Identifier): IdentifierResponse {
        return IdentifierResponse(
            id = identifier.id!!,
            identifier = identifier.identifier!!,
            type = identifier.type!!,
            verificationStatus = identifier.verificationStatus,
            verificationNotes = identifier.verificationNotes,
            extData = identifier.extData,
            createdAt = identifier.createdAt!!,
            updatedAt = identifier.updatedAt!!,
            createdBy = identifier.createdBy,
            updatedBy = identifier.updatedBy
        )
    }
}
