package com.nivasafinance.features.income.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class IncomeDetailsResponse(
    val id: UUID,
    val entityType: String,
    val entityId: UUID,
    val employmentType: String,
    val employerName: String?,
    val employerType: String?,
    val jobTitle: String?,
    val department: String?,
    val location: String?,
    val salary: BigDecimal?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String?,
    val updatedBy: String?
)
