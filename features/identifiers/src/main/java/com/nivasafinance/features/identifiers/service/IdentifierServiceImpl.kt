package com.nivasafinance.features.identifiers.service

import com.nivasafinance.features.identifiers.dto.IdentifierRequest
import com.nivasafinance.features.identifiers.dto.IdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import com.nivasafinance.features.identifiers.entity.Identifier
import com.nivasafinance.features.identifiers.enum.EntityType
import com.nivasafinance.features.identifiers.exception.IdentifierExceptionFactory
import com.nivasafinance.features.identifiers.repository.IdentifierRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class IdentifierServiceImpl(
    private val identifierRepositoryWrapper: IdentifierRepositoryWrapper,
    private val messageSource: MessageSource
) : IdentifierService {

    private fun validateEntityType(entityType: String) {
        try {
            EntityType.valueOf(entityType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IdentifierExceptionFactory.unsupportedEntityType(entityType, messageSource)
        }
    }

    override fun createIdentifierByEntity(entityType: String, entityId: UUID, identifierRequest: IdentifierRequest): IdentifierResponse {
        validateEntityType(entityType)
        val identifier = Identifier(
            entityType = entityType,
            entityId = entityId,
            identifier = identifierRequest.identifier,
            type = identifierRequest.type.name,
            verificationStatus = identifierRequest.verificationStatus,
            verificationNotes = identifierRequest.verificationNotes,
            extData = identifierRequest.extData
        )
        val savedIdentifier = identifierRepositoryWrapper.saveWithException(identifier)
        return toIdentifierResponse(savedIdentifier)
    }

    override fun updateIdentifierByEntity(entityType: String, entityId: UUID, identifierId: UUID, identifierUpdateRequest: IdentifierUpdateRequest): IdentifierResponse {
        validateEntityType(entityType)
        val existingIdentifier = identifierRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId).firstOrNull { it.id == identifierId }
        if (existingIdentifier == null) {
            throw IdentifierExceptionFactory.notFound(identifierId, messageSource)
        }
        identifierUpdateRequest.identifier?.let { 
            existingIdentifier.identifier = it
        }
        identifierUpdateRequest.type?.let { existingIdentifier.type = it.name }
        identifierUpdateRequest.verificationStatus?.let { existingIdentifier.verificationStatus = it }
        identifierUpdateRequest.verificationNotes?.let { existingIdentifier.verificationNotes = it }
        identifierUpdateRequest.extData?.let { existingIdentifier.extData = it }
        val savedIdentifier = identifierRepositoryWrapper.saveWithException(existingIdentifier)
        return toIdentifierResponse(savedIdentifier)
    }

    override fun deleteIdentifierByEntity(entityType: String, entityId: UUID, identifierId: UUID) {
        validateEntityType(entityType)
        val existingIdentifier = identifierRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId).firstOrNull { it.id == identifierId }
        if (existingIdentifier == null) {
            throw IdentifierExceptionFactory.notFound(identifierId, messageSource)
        }
        identifierRepositoryWrapper.deleteByIdWithException(identifierId)
    }

    override fun getIdentifiersByEntity(entityType: String, entityId: UUID): List<IdentifierResponse> {
        validateEntityType(entityType)
        val identifiers = identifierRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId)
        return identifiers.map { toIdentifierResponse(it) }
    }

    private fun toIdentifierResponse(identifier: Identifier): IdentifierResponse {
        return IdentifierResponse(
            id = identifier.id!!,
            entityId = identifier.entityId!!,
            entityType = EntityType.valueOf(identifier.entityType!!),
            identifier = identifier.identifier!!,
            type = com.nivasafinance.features.identifiers.enum.IdentifierType.valueOf(identifier.type!!),
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
