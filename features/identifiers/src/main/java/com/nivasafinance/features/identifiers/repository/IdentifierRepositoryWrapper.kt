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


    fun findByIdWithException(identifierId: UUID): Identifier {
        return try {
            identifierRepository.findById(identifierId).orElseThrow {
                IdentifierExceptionFactory.notFound(identifierId, messageSource)
            }
        } catch (e: Exception) {
            throw IdentifierExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllWithException(): List<Identifier> {
        return try {
            identifierRepository.findAll()
        } catch (e: Exception) {
            throw IdentifierExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}
