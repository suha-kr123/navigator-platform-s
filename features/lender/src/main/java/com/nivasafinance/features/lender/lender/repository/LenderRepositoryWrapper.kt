package com.nivasafinance.features.lender.lender.repository

import com.nivasafinance.features.lender.lender.entity.Lender
import com.nivasafinance.features.lender.lender.enum.LenderStatus
import com.nivasafinance.features.lender.lender.exception.LenderExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LenderRepositoryWrapper(
    private val lenderRepository: LenderRepository,
    private val messageSource: MessageSource,
) {
    fun saveWithException(lender: Lender): Lender {
        return try {
            lenderRepository.save(lender)
        } catch (e: Exception) {
            throw LenderExceptionFactory.createFailed(messageSource)
        }
    }

    fun findByIdWithException(id: UUID): Lender {
        return lenderRepository.findById(id).orElseThrow {
            LenderExceptionFactory.lenderNotFound(id, messageSource)
        }
    }

    fun findByKeyWithException(key: String): Lender {
        return lenderRepository.findByKey(key) ?: throw LenderExceptionFactory.lenderNotFound(key, messageSource)
    }

    fun findAll(): List<Lender> = lenderRepository.findAll()

    fun findAllByStatus(status: LenderStatus): List<Lender> = lenderRepository.findByStatus(status)

    fun deleteByIdWithException(id: UUID) {
        if (!lenderRepository.existsById(id)) {
            throw LenderExceptionFactory.lenderNotFound(id, messageSource)
        }
        lenderRepository.deleteById(id)
    }
}
