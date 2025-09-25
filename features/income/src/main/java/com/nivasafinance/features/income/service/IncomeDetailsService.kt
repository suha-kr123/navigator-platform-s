package com.nivasafinance.features.income.service

import com.nivasafinance.features.income.dto.IncomeDetailsRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import java.util.UUID

interface IncomeDetailsService {
    fun createIncomeDetails(incomeDetailsRequest: IncomeDetailsRequest): IncomeDetailsResponse
    fun updateIncomeDetails(incomeDetailsId: UUID, incomeDetailsUpdateRequest: IncomeDetailsUpdateRequest): IncomeDetailsResponse
    fun deleteIncomeDetails(incomeDetailsId: UUID)
    fun getIncomeDetailsById(incomeDetailsId: UUID): IncomeDetailsResponse
    fun getAllIncomeDetails(): List<IncomeDetailsResponse>
}
