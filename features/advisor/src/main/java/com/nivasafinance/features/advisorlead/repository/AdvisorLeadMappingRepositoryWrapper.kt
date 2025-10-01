package com.nivasafinance.features.advisorlead.repository

import com.nivasafinance.features.advisorlead.entity.AdvisorLeadMapping
import com.nivasafinance.features.advisorlead.exception.AdvisorLeadMappingExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorLeadMappingRepositoryWrapper(
    private val advisorLeadMappingRepository: AdvisorLeadMappingRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(advisorLeadMapping: AdvisorLeadMapping): AdvisorLeadMapping {
        return try {
            advisorLeadMappingRepository.save(advisorLeadMapping)
        } catch (e: DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.createFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findByIdWithException(id: UUID): AdvisorLeadMapping {
        return try {
            advisorLeadMappingRepository.findById(id).orElseThrow {
                AdvisorLeadMappingExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findAllByAdvisorIdWithException(advisorId: UUID, pageable: Pageable): Page<AdvisorLeadMapping> {
        return try {
            advisorLeadMappingRepository.findAllByAdvisorId(advisorId, pageable)
        } catch (e: DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            advisorLeadMappingRepository.deleteById(id)
        } catch (e: DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.deleteFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }
}
