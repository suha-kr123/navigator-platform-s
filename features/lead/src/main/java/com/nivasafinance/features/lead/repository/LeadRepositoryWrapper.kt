package com.nivasafinance.features.lead.repository

import com.nivasafinance.features.lead.dto.LeadSummaryDTO
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.exception.LeadNotFoundException
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
            // Log the actual exception for debugging
            println("Error saving lead: ${e.message}")
            e.printStackTrace()

            // Determine if this is a create or update operation
            if (lead.id == null) {
                throw LeadExceptionFactory.createFailed(messageSource)
            } else {
                throw LeadExceptionFactory.updateFailed(messageSource)
            }
        }
    }

    fun findByIdWithException(id: UUID): Lead {
        return try {
            leadRepository.findById(id).orElseThrow {
                LeadExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: LeadNotFoundException) {
            throw e
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
    
    /**
     * Optimized count method using custom query for better performance.
     */
    fun countLeadsWithException(): Long {
        return try {
            leadRepository.countLeads()
        } catch (e: Exception) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            leadRepository.deleteById(id)
        } catch (e: Exception) {
            throw LeadExceptionFactory.deleteFailed(messageSource)
        }
    }

    /**
     * Fetches lead summary data using optimized custom query.
     * More efficient than fetching full Lead entities for listing views.
     */
    fun findAllSummaryDataWithException(pageable: Pageable): Page<LeadSummaryDTO> {
        return try {
            leadRepository.findAllSummaryData(pageable)
        } catch (e: Exception) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findSummaryDataByPhoneNumberWithException(phoneNumber: String): List<LeadSummaryDTO> {
        return try {
            leadRepository.findSummaryDataByPhoneNumber(phoneNumber)
        } catch (e: Exception) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}
