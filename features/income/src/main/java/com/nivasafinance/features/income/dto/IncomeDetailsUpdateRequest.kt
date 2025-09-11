package com.nivasafinance.features.income.dto

import annotations.NoArg
import com.nivasafinance.features.income.enum.EmployerType
import java.math.BigDecimal

@NoArg
data class IncomeDetailsUpdateRequest(
    var employerName: String? = null,
    var employerType: EmployerType? = null,
    var jobTitle: String? = null,
    var department: String? = null,
    var location: String? = null,
    var salary: BigDecimal? = null,
    var documents: Map<String, Any>? = null,
    var extData: Map<String, Any>? = null
)
