package com.nivasafinance.features.income.repository

import com.nivasafinance.features.income.entity.IncomeDetails
import com.nivasafinance.features.income.exception.IncomeDetailsExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class IncomeDetailsRepositoryWrapper(
    private val incomeDetailsRepository: IncomeDetailsRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(incomeDetails: IncomeDetails): IncomeDetails {
        return try {
            incomeDetailsRepository.save(incomeDetails)
        } catch (e: RuntimeException) {
            throw IncomeDetailsExceptionFactory.createFailed(messageSource)
        }
    }

    fun deleteByIdWithException(incomeDetailsId: UUID) {
        try {
            incomeDetailsRepository.deleteById(incomeDetailsId)
        } catch (e: RuntimeException) {
            throw IncomeDetailsExceptionFactory.deleteFailed(messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<IncomeDetails> {
        return try {
            incomeDetailsRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
        } catch (e: Exception) {
            throw IncomeDetailsExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}
