package com.nivasafinance.features.income.service

import com.nivasafinance.features.income.dto.IncomeDetailsRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import java.util.UUID

interface IncomeDetailsService {
    fun createIncomeDetailsByEntity(entityType: String, entityId: UUID, incomeDetailsRequest: IncomeDetailsRequest): IncomeDetailsResponse
    fun updateIncomeDetailsByEntity(entityType: String, entityId: UUID, incomeDetailsId: UUID, incomeDetailsUpdateRequest: IncomeDetailsUpdateRequest): IncomeDetailsResponse
    fun deleteIncomeDetailsByEntity(entityType: String, entityId: UUID, incomeDetailsId: UUID)
    fun getIncomeDetailsByEntity(entityType: String, entityId: UUID): List<IncomeDetailsResponse>
}
