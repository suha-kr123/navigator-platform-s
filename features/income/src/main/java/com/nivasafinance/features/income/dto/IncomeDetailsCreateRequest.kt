package com.nivasafinance.features.income.dto

import annotations.NoArg
import com.nivasafinance.features.income.enum.EmployerType
import java.math.BigDecimal
import java.util.UUID

@NoArg
data class IncomeDetailsCreateRequest(
    var entityId: UUID,
    var entityType: String,
    var employmentType: String,
    var employerName: String? = null,
    var employerType: EmployerType? = null,
    var jobTitle: String? = null,
    var department: String? = null,
    var location: String? = null,
    var salary: BigDecimal? = null,
    var documents: Map<String, Any>? = null,
    var extData: Map<String, Any>? = null
)
