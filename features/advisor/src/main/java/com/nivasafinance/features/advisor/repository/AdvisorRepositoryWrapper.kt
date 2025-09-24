package com.nivasafinance.features.advisor.repository

import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdvisorRepositoryWrapper(
    private val advisorRepository: AdvisorRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(advisor: Advisor): Advisor {
        return try {
            advisorRepository.save(advisor)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorExceptionFactory.createFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findByIdWithException(id: UUID): Advisor {
        return try {
            advisorRepository.findById(id).orElseThrow {
                AdvisorExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: AdvisorNotFoundException) {
            throw e
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun findAllWithException(pageable: Pageable): Page<Advisor> {
        return try {
            advisorRepository.findAll(pageable)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun countWithException(): Long {
        return try {
            advisorRepository.count()
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            advisorRepository.deleteById(id)
        } catch (e: org.springframework.dao.DataAccessException) {
            val exception = AdvisorExceptionFactory.deleteFailed(messageSource)
            exception.initCause(e)
            throw exception
        }
    }
}
