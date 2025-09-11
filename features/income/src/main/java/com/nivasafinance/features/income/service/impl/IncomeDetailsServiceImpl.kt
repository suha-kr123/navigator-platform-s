package com.nivasafinance.features.income.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.income.dto.IncomeDetailsCreateRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import com.nivasafinance.features.income.entity.IncomeDetails
import com.nivasafinance.features.income.repository.IncomeDetailsRepository
import com.nivasafinance.features.income.service.IncomeDetailsService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class IncomeDetailsServiceImpl(
    private val incomeDetailsRepository: IncomeDetailsRepository
) : IncomeDetailsService, BaseNavigatorService() {

    @CacheEvict(value = ["incomeDetails"], allEntries = true)
    @Transactional
    override fun createIncomeDetails(request: IncomeDetailsCreateRequest): IncomeDetailsResponse {
        val incomeDetails = IncomeDetails(
            entityId = request.entityId,
            entityType = request.entityType,
            employmentType = request.employmentType,
            employerName = request.employerName,
            employerType = request.employerType,
            jobTitle = request.jobTitle,
            department = request.department,
            location = request.location,
            salary = request.salary,
            documents = request.documents,
            extData = request.extData
        )
        val savedIncomeDetails = incomeDetailsRepository.save(incomeDetails)
        return IncomeDetailsResponse.fromIncomeDetails(savedIncomeDetails)
    }

    @Cacheable(value = ["incomeDetails"], key = "#employmentId")
    @Transactional(readOnly = true)
    override fun getIncomeDetails(employmentId: UUID): IncomeDetailsResponse? {
        val incomeDetails = incomeDetailsRepository.findById(employmentId).orElse(null)
        return incomeDetails?.let { IncomeDetailsResponse.fromIncomeDetails(it) }
    }

    @CacheEvict(value = ["incomeDetails"], key = "#employmentId")
    @Transactional
    override fun updateIncomeDetails(employmentId: UUID, request: IncomeDetailsUpdateRequest): IncomeDetailsResponse {
        val incomeDetails = incomeDetailsRepository.findById(employmentId)
            .orElseThrow { RuntimeException("Income details not found with id: $employmentId") }

        // Update fields if provided
        request.employerName?.let { incomeDetails.employerName = it }
        request.employerType?.let { incomeDetails.employerType = it }
        request.jobTitle?.let { incomeDetails.jobTitle = it }
        request.department?.let { incomeDetails.department = it }
        request.location?.let { incomeDetails.location = it }
        request.salary?.let { incomeDetails.salary = it }
        request.documents?.let { incomeDetails.documents = it }
        request.extData?.let { incomeDetails.extData = it }

        val updatedIncomeDetails = incomeDetailsRepository.save(incomeDetails)
        return IncomeDetailsResponse.fromIncomeDetails(updatedIncomeDetails)
    }

    @CacheEvict(value = ["incomeDetails"], key = "#employmentId")
    @Transactional
    override fun deleteIncomeDetails(employmentId: UUID) {
        incomeDetailsRepository.deleteById(employmentId)
    }
}
