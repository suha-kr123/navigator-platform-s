package com.nivasafinance.features.income.service

import com.nivasafinance.features.income.dto.IncomeDetailsRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import com.nivasafinance.features.income.entity.IncomeDetails
import com.nivasafinance.features.income.enum.EntityType
import com.nivasafinance.features.income.exception.IncomeDetailsExceptionFactory
import com.nivasafinance.features.income.repository.IncomeDetailsRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class IncomeDetailsServiceImpl(
    private val incomeDetailsRepositoryWrapper: IncomeDetailsRepositoryWrapper,
    private val messageSource: MessageSource
) : IncomeDetailsService {

    private fun validateEntityType(entityType: String) {
        try {
            EntityType.valueOf(entityType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IncomeDetailsExceptionFactory.unsupportedEntityType(entityType, messageSource)
        }
    }

    override fun createIncomeDetailsByEntity(entityType: String, entityId: UUID, incomeDetailsRequest: IncomeDetailsRequest): IncomeDetailsResponse {
        validateEntityType(entityType)
        val incomeDetails = IncomeDetails(
            entityType = entityType,
            entityId = entityId,
            employmentType = incomeDetailsRequest.employmentType,
            employerName = incomeDetailsRequest.employerName,
            employerType = incomeDetailsRequest.employerType,
            jobTitle = incomeDetailsRequest.jobTitle,
            department = incomeDetailsRequest.department,
            location = incomeDetailsRequest.location,
            salary = incomeDetailsRequest.salary,
            verificationStatus = incomeDetailsRequest.verificationStatus,
            verificationNotes = incomeDetailsRequest.verificationNotes
        )
        val savedIncomeDetails = incomeDetailsRepositoryWrapper.saveWithException(incomeDetails)
        return toIncomeDetailsResponse(savedIncomeDetails)
    }

    override fun updateIncomeDetailsByEntity(entityType: String, entityId: UUID, incomeDetailsId: UUID, incomeDetailsUpdateRequest: IncomeDetailsUpdateRequest): IncomeDetailsResponse {
        validateEntityType(entityType)
        val existingIncomeDetails = incomeDetailsRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId).firstOrNull { it.id == incomeDetailsId }
        if (existingIncomeDetails == null) {
            throw IncomeDetailsExceptionFactory.notFound(incomeDetailsId, messageSource)
        }
        existingIncomeDetails.employmentType = incomeDetailsUpdateRequest.employmentType
        existingIncomeDetails.employerName = incomeDetailsUpdateRequest.employerName
        existingIncomeDetails.jobTitle = incomeDetailsUpdateRequest.jobTitle
        existingIncomeDetails.department = incomeDetailsUpdateRequest.department
        existingIncomeDetails.location = incomeDetailsUpdateRequest.location
        existingIncomeDetails.employerType = incomeDetailsUpdateRequest.employerType
        existingIncomeDetails.salary = incomeDetailsUpdateRequest.salary
        existingIncomeDetails.verificationStatus = incomeDetailsUpdateRequest.verificationStatus
        existingIncomeDetails.verificationNotes = incomeDetailsUpdateRequest.verificationNotes
        val savedIncomeDetails = incomeDetailsRepositoryWrapper.saveWithException(existingIncomeDetails)
        return toIncomeDetailsResponse(savedIncomeDetails)
    }

    override fun deleteIncomeDetailsByEntity(entityType: String, entityId: UUID, incomeDetailsId: UUID) {
        validateEntityType(entityType)
        val existingIncomeDetails = incomeDetailsRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId).firstOrNull { it.id == incomeDetailsId }
        if (existingIncomeDetails == null) {
            throw IncomeDetailsExceptionFactory.notFound(incomeDetailsId, messageSource)
        }
        incomeDetailsRepositoryWrapper.deleteByIdWithException(incomeDetailsId)
    }

    override fun getIncomeDetailsByEntity(entityType: String, entityId: UUID): List<IncomeDetailsResponse> {
        validateEntityType(entityType)
        val incomeDetails = incomeDetailsRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId)
        return incomeDetails.map { toIncomeDetailsResponse(it) }
    }

    private fun toIncomeDetailsResponse(incomeDetails: IncomeDetails): IncomeDetailsResponse {
        return IncomeDetailsResponse(
            id = incomeDetails.id!!,
            entityType = incomeDetails.entityType!!,
            entityId = incomeDetails.entityId!!,
            employmentType = incomeDetails.employmentType!!,
            employerName = incomeDetails.employerName,
            employerType = incomeDetails.employerType,
            jobTitle = incomeDetails.jobTitle,
            department = incomeDetails.department,
            location = incomeDetails.location,
            salary = incomeDetails.salary,
            verificationStatus = incomeDetails.verificationStatus,
            verificationNotes = incomeDetails.verificationNotes,
            createdAt = incomeDetails.createdAt!!,
            updatedAt = incomeDetails.updatedAt!!,
            createdBy = incomeDetails.createdBy,
            updatedBy = incomeDetails.updatedBy
        )
    }
}
