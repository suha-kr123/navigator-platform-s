package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.PersonData
import com.nivasafinance.features.lead.entity.RequestedAmountRange
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class LeadResponse(
    val id: UUID,
    val requestedAmountRange: RequestedAmountRange?,
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
    val dateOfBirth: LocalDate?,
    val gender: String?,

    // Phone Numbers
    val mobileNumbers: List<MobileNumberDetails>?,

    // Lead Relationship Data
    val leadPersonType: String?,
    val relationshipToPrimary: String?,

    // Additional data
    val extData: Map<String, Any>?
)

/**
 * Extension function to convert Lead entity to LeadResponse.
 * This keeps the mapping logic close to the DTO and prevents service classes from becoming huge.
 */
fun Lead.toLeadResponse(personRepository: PersonRepository): LeadResponse {
    return LeadResponse(
        id = id ?: UUID.randomUUID(),
        requestedAmountRange = requestedAmountRange,
        purpose = purpose,
        productCode = productCode,
        currentStage = currentStage,
        preliminaryInformation = preliminaryInformation?.get("data") as? LeadPreliminaryInformation,
        sourcingChannel = sourcingChannel,
        extData = extData,
        taskData = taskData?.let { taskDataList ->
            taskDataList.associate { taskData ->
                taskData.taskId to mapOf()
            }
        },
        createdAt = createdAt ?: LocalDateTime.now(),
        createdBy = createdBy,
        updatedAt = updatedAt ?: LocalDateTime.now(),
        updatedBy = updatedBy,
        leadPersons = personData?.map { personData ->
            personData.toLeadPersonsResponse(id!!, personRepository)
        }
    )
}

/**
 * Extension function to convert PersonData to LeadPersonsResponse.
 */
fun PersonData.toLeadPersonsResponse(leadId: UUID, personRepository: PersonRepository): LeadPersonsResponse {
    // Fetch the actual person from database
    val person = personRepository.findById(personId).orElse(null)

    return LeadPersonsResponse(
        leadId = leadId,
        personId = personId,
        firstName = person?.firstName,
        middleName = person?.middleName,
        lastName = person?.lastName,
        dateOfBirth = person?.dateOfBirth,
        gender = person?.gender?.name,
        mobileNumbers = person?.mobileNumbers,
        leadPersonType = leadPersonType.value,
        relationshipToPrimary = relationshipToPrimary,
        extData = person?.extData
    )
}
