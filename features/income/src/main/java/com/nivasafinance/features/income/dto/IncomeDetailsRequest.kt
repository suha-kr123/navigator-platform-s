package com.nivasafinance.features.income.dto

import java.math.BigDecimal

data class IncomeDetailsRequest(
    val employmentType: String,
    val employerName: String?,
    val employerType: String?,
    val jobTitle: String?,
    val department: String?,
    val location: String?,
    val salary: BigDecimal?,
    val verificationStatus: String?,
    val verificationNotes: String?
)
