package com.nivasafinance.features.person.dto

import annotations.NoArg
import com.nivasafinance.features.person.enum.EmployerType
import com.nivasafinance.features.person.enum.EmploymentType
import java.math.BigDecimal

@NoArg
data class EmploymentDetailsUpdateRequest(
    var employerName: String? = null,
    var employerType: EmployerType? = null,
    var jobTitle: String? = null,
    var department: String? = null,
    var employmentType: EmploymentType? = null,
    var location: String? = null,
    var salary: BigDecimal? = null,
    var documents: Map<String, Any>? = null,
    var extData: Map<String, Any>? = null
)
