package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import java.time.LocalDateTime
import java.util.UUID

data class PersonResponse(
    val id: UUID,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val dateOfBirth: String?,
    val gender: String?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
