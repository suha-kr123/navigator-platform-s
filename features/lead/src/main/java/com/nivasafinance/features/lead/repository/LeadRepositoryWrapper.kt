package com.nivasafinance.features.lead.repository

import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadRepositoryWrapper(
    private val leadRepository: LeadRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(lead: Lead): Lead {
        return try {
            leadRepository.save(lead)
        } catch (e: Exception) {
            throw LeadExceptionFactory.createFailed(messageSource)
        }
    }

    fun findByIdWithException(id: UUID): Lead {
        return try {
            leadRepository.findById(id).orElseThrow {
                LeadExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: Exception) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllWithException(pageable: Pageable): Page<Lead> {
        return try {
            leadRepository.findAll(pageable)
        } catch (e: Exception) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun countWithException(): Long {
        return try {
            leadRepository.count()
        } catch (e: Exception) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteByIdWithException(id: UUID) {
        return try {
            leadRepository.deleteById(id)
        } catch (e: Exception) {
            throw LeadExceptionFactory.deleteFailed(messageSource)
        }
    }
}
