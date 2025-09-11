package com.nivasafinance.features.income.dto

import annotations.NoArg
import com.nivasafinance.features.income.enum.EmployerType
import java.math.BigDecimal
import java.util.UUID

@NoArg
data class IncomeDetailsResponse(
    val employmentId: UUID?,
    val entityId: UUID?,
    val entityType: String?,
    val employmentType: String?,
    val employerName: String? = null,
    val employerType: EmployerType? = null,
    val jobTitle: String? = null,
    val department: String? = null,
    val location: String? = null,
    val salary: BigDecimal? = null,
    val documents: Map<String, Any>? = null,
    val extData: Map<String, Any>? = null
) {
    companion object {
        fun fromIncomeDetails(
            incomeDetails: com.nivasafinance.features.income.entity.IncomeDetails
        ): IncomeDetailsResponse {
            return IncomeDetailsResponse(
                employmentId = incomeDetails.employmentId,
                entityId = incomeDetails.entityId,
                entityType = incomeDetails.entityType,
                employmentType = incomeDetails.employmentType,
                employerName = incomeDetails.employerName,
                employerType = incomeDetails.employerType,
                jobTitle = incomeDetails.jobTitle,
                department = incomeDetails.department,
                location = incomeDetails.location,
                salary = incomeDetails.salary,
                documents = incomeDetails.documents,
                extData = incomeDetails.extData
            )
        }
    }
}
