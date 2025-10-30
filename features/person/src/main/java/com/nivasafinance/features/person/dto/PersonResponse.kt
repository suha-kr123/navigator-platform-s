package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import java.time.LocalDate
import java.time.LocalDateTime

data class PersonResponse(
    val id: Long,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val dateOfBirth: LocalDate?,
    val gender: String?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
