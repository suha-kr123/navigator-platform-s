package com.nivasafinance.features.identifiers.repository

import com.nivasafinance.features.identifiers.entity.Identifier
import com.nivasafinance.features.identifiers.exception.IdentifierExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class IdentifierRepositoryWrapper(
    private val identifierRepository: IdentifierRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(identifier: Identifier): Identifier {
        return try {
            identifierRepository.save(identifier)
        } catch (e: RuntimeException) {
            throw IdentifierExceptionFactory.createFailed(messageSource)
        }
    }

    fun deleteByIdWithException(identifierId: UUID) {
        try {
            identifierRepository.deleteById(identifierId)
        } catch (e: RuntimeException) {
            throw IdentifierExceptionFactory.deleteFailed(messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<Identifier> {
        return try {
            identifierRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
        } catch (e: Exception) {
            throw IdentifierExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}
