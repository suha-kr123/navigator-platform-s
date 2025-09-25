package com.nivasafinance.features.income.service

import com.nivasafinance.features.income.dto.IncomeDetailsRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import com.nivasafinance.features.income.entity.IncomeDetails
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

    override fun createIncomeDetails(incomeDetailsRequest: IncomeDetailsRequest): IncomeDetailsResponse {
        val incomeDetails = IncomeDetails(
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

    override fun updateIncomeDetails(incomeDetailsId: UUID, incomeDetailsUpdateRequest: IncomeDetailsUpdateRequest): IncomeDetailsResponse {
        val existingIncomeDetails = incomeDetailsRepositoryWrapper.findByIdWithException(incomeDetailsId)
        
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

    override fun deleteIncomeDetails(incomeDetailsId: UUID) {
        incomeDetailsRepositoryWrapper.deleteByIdWithException(incomeDetailsId)
    }

    override fun getIncomeDetailsById(incomeDetailsId: UUID): IncomeDetailsResponse {
        val incomeDetails = incomeDetailsRepositoryWrapper.findByIdWithException(incomeDetailsId)
        return toIncomeDetailsResponse(incomeDetails)
    }

    override fun getAllIncomeDetails(): List<IncomeDetailsResponse> {
        val incomeDetails = incomeDetailsRepositoryWrapper.findAllWithException()
        return incomeDetails.map { toIncomeDetailsResponse(it) }
    }

    private fun toIncomeDetailsResponse(incomeDetails: IncomeDetails): IncomeDetailsResponse {
        return IncomeDetailsResponse(
            id = incomeDetails.id!!,
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

