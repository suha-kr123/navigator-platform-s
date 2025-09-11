package com.nivasafinance.features.income.service

import com.nivasafinance.features.income.dto.IncomeDetailsCreateRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import java.util.UUID

interface IncomeDetailsService {
    fun createIncomeDetails(request: IncomeDetailsCreateRequest): IncomeDetailsResponse
    fun getIncomeDetails(employmentId: UUID): IncomeDetailsResponse?
    fun updateIncomeDetails(employmentId: UUID, request: IncomeDetailsUpdateRequest): IncomeDetailsResponse
    fun deleteIncomeDetails(employmentId: UUID)
}
