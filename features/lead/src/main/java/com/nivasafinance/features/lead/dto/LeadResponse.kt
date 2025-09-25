package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class LeadResponse(
    val id: UUID,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val currentStage: String?,
    val sourcingChannel: String?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val extData: Map<String, Any>?,
    val taskData: Map<UUID, Map<String, List<UUID>>>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
    val leadPersons: List<LeadPersonsResponse>?
)

data class LeadPersonsResponse(
    val leadId: UUID,
    val personId: UUID? = null,
    
    // Personal Information
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val email: String?,
    val dateOfBirth: String?,
    val gender: String?,
    
    // Phone Numbers
    val mobileNumbers: List<MobileNumberDetails>?,
    
    // Lead Relationship Data
    val applicantType: String?, 
    val relationshipToPrimary: String?,
    val isPrimary: Boolean? = false,
    val tags: List<String>?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    
    // Additional data
    val extData: Map<String, Any>?
)
