package com.nivasafinance.features.advisorleadmapping.repository

import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingExceptionFactory
import org.springframework.context.MessageSource
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
        } catch (e: org.springframework.dao.DataAccessException) {
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
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findAllByAdvisorIdWithException(advisorId: UUID, pageable: Pageable): Page<AdvisorLeadMapping> {
        return try {
            advisorLeadMappingRepository.findAllByAdvisorId(advisorId, pageable)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            advisorLeadMappingRepository.deleteById(id)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorLeadMappingExceptionFactory.deleteFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }
}
