package com.nivasafinance.features.leadpersons.repository

import com.nivasafinance.features.leadpersons.entity.LeadPersons
import com.nivasafinance.features.leadpersons.exception.LeadPersonsExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class LeadPersonsRepositoryWrapper(
    private val leadPersonsRepository: LeadPersonsRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(leadPerson: LeadPersons): LeadPersons {
        return try {
            leadPersonsRepository.save(leadPerson)
        } catch (e: Exception) {
            if (leadPerson.id != null) {
                throw LeadPersonsExceptionFactory.updateFailed(messageSource)
            } else {
                throw LeadPersonsExceptionFactory.createFailed(messageSource)
            }
        }
    }

    fun findByIdWithException(id: UUID): LeadPersons {
        return try {
            leadPersonsRepository.findById(id).orElseThrow {
                LeadPersonsExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: Exception) {
            throw LeadPersonsExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllWithException(pageable: Pageable): Page<LeadPersons> {
        return try {
            leadPersonsRepository.findAll(pageable)
        } catch (e: Exception) {
            throw LeadPersonsExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findByLeadIdWithException(leadId: UUID): List<LeadPersons> {
        return try {
            leadPersonsRepository.findByLeadId(leadId)
        } catch (e: Exception) {
            throw LeadPersonsExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findByPersonIdWithException(personId: UUID): List<LeadPersons> {
        return try {
            leadPersonsRepository.findByPersonId(personId)
        } catch (e: Exception) {
            throw LeadPersonsExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun existsByLeadIdAndPersonIdWithException(leadId: UUID, personId: UUID): Boolean {
        return try {
            leadPersonsRepository.existsByLeadIdAndPersonId(leadId, personId)
        } catch (e: Exception) {
            throw LeadPersonsExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteWithException(id: UUID) {
        try {
            leadPersonsRepository.deleteById(id)
        } catch (e: Exception) {
            throw LeadPersonsExceptionFactory.deleteFailed(messageSource)
        }
    }
}
