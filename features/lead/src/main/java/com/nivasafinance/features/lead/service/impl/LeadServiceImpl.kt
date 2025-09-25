package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadPersonsResponse
import com.nivasafinance.features.lead.dto.LeadPreliminaryInformation
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.PersonData
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.stages.service.StageService
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.repository.PersonRepository
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class LeadServiceImpl(
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val stageRepositoryWrapper: StageRepositoryWrapper,
    private val stageDefinitionRepositoryWrapper: StageDefinitionRepositoryWrapper,
    private val stageService: StageService,
    private val personRepository: PersonRepository,
    private val messageSource: MessageSource
) : LeadService {

    override fun createLead(leadCreateRequest: LeadCreateRequest): LeadResponse {
        LeadExceptionFactory.validateLeadForCreation(
            leadCreateRequest.requestedAmount,
            leadCreateRequest.purpose,
            leadCreateRequest.productCode,
            null,
            leadCreateRequest.sourcingChannel,
            messageSource
        )
        
        // Handle person creation
        val personData = if (leadCreateRequest.leadPersons != null) {
            validateAndCreatePersons(leadCreateRequest.leadPersons)
        } else {
            emptyList()
        }
        
        val lead = toLead(leadCreateRequest, personData)
        val savedLead = leadRepositoryWrapper.saveWithException(lead)

        val stages = createStagesForLead(leadCreateRequest.pipelineKey)

        // Update the lead with the stage IDs
        val stageIds = stages.map { it.id }
        val updatedLead = savedLead.copy(stageIds = stageIds)
        val finalLead = leadRepositoryWrapper.saveWithException(updatedLead)

        return toLeadResponse(finalLead)
    }

    override fun updateLead(id: UUID, leadUpdateRequest: com.nivasafinance.features.lead.dto.LeadUpdateRequest): LeadResponse {
        val existingLead = leadRepositoryWrapper.findByIdWithException(id)

        // Create a mutable copy to update only provided fields (no person data changes in basic update)
        val updatedLead = existingLead.copy(
            requestedAmount = leadUpdateRequest.requestedAmount ?: existingLead.requestedAmount,
            purpose = leadUpdateRequest.purpose ?: existingLead.purpose,
            productCode = leadUpdateRequest.productCode ?: existingLead.productCode,
            pipelineKey = leadUpdateRequest.pipelineKey ?: existingLead.pipelineKey,
            currentStage = leadUpdateRequest.currentStage ?: existingLead.currentStage,
            sourcingChannel = leadUpdateRequest.sourcingChannel ?: existingLead.sourcingChannel,
            preliminaryInformation = if (leadUpdateRequest.preliminaryInformation != null) {
                leadUpdateRequest.preliminaryInformation
            } else {
                existingLead.preliminaryInformation
            },
            extData = leadUpdateRequest.extData ?: existingLead.extData
        )

        val savedLead = leadRepositoryWrapper.saveWithException(updatedLead)
        return toLeadResponse(savedLead)
    }

    override fun getLeadById(id: UUID): LeadResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(id)
        return toLeadResponse(lead)
    }

    override fun getAllLeads(paginationRequest: PaginationRequest): PaginatedResponse<LeadResponse> {
        val pageable = org.springframework.data.domain.PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )
        val leadPage = leadRepositoryWrapper.findAllWithException(pageable)
        val leadResponses = leadPage.content.map { toLeadResponse(it) }

        val totalPages = if (leadPage.totalElements == 0L) 0 else ((leadPage.totalElements - 1) / paginationRequest.limit + 1).toInt()
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = leadResponses,
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadPage.totalElements,
                totalPages = totalPages,
                currentPage = currentPage,
                hasNext = currentPage < totalPages - 1,
                hasPrevious = currentPage > 0
            )
        )
    }

    private fun toLead(leadCreateRequest: LeadCreateRequest, personData: List<PersonData>): Lead {
        return Lead(
            requestedAmount = leadCreateRequest.requestedAmount,
            purpose = leadCreateRequest.purpose,
            productCode = leadCreateRequest.productCode,
            pipelineKey = leadCreateRequest.pipelineKey,
            currentStage = leadCreateRequest.currentStage,
            sourcingChannel = leadCreateRequest.sourcingChannel,
            preliminaryInformation = leadCreateRequest.preliminaryInformation?.let {
                mapOf("data" to it)
            },
            extData = leadCreateRequest.extData,
            personData = personData
        )
    }

    private fun toLeadResponse(lead: Lead): LeadResponse {
        return LeadResponse(
            id = lead.id ?: UUID.randomUUID(),
            requestedAmount = lead.requestedAmount,
            purpose = lead.purpose,
            productCode = lead.productCode,
            currentStage = lead.currentStage,
            preliminaryInformation = lead.preliminaryInformation?.get(
                "data"
            ) as? LeadPreliminaryInformation,
            sourcingChannel = lead.sourcingChannel,
            extData = lead.extData,
            taskData = lead.taskData?.let { taskDataList ->
                taskDataList.associate { taskData ->
                    taskData.taskId to mapOf(
                        "documentIds" to taskData.documentIds,
                        "notesIds" to taskData.notesIds,
                        "callIds" to taskData.callIds
                    )
                }
            },
            createdAt = lead.createdAt ?: java.time.LocalDateTime.now(),
            createdBy = lead.createdBy,
            updatedAt = lead.updatedAt ?: java.time.LocalDateTime.now(),
            updatedBy = lead.updatedBy,
            leadPersons = lead.personData?.map { personData ->
                fetchPersonDetails(personData, lead.id!!)
            }
        )
    }

    private fun createStagesForLead(pipelineKey: String?): List<com.nivasafinance.features.stages.dto.StageResponse> {
        if (pipelineKey.isNullOrBlank()) {
            return emptyList()
        }

        val stageDefinitions = stageDefinitionRepositoryWrapper.findByPipelineKeyWithException(pipelineKey)
        val stages = mutableListOf<com.nivasafinance.features.stages.dto.StageResponse>()

        stageDefinitions.forEach { stageDefinition ->
            // Use the first valid outcome from the stage definition
            val validOutcome = stageDefinition.possibleOutcomes?.firstOrNull() ?: "PENDING"
            val stageRequest = StageRequest(
                stageDefinitionKey = stageDefinition.key,
                outcome = validOutcome,
                assignedTo = null
            )
            val savedStage = stageService.createStage(stageRequest)
            stages.add(savedStage)
        }

        return stages
    }

    // Person handling methods
    private fun validateAndCreatePersons(personRequests: List<com.nivasafinance.features.lead.dto.PersonRequest>): List<PersonData> {
        val primaryPhones = mutableSetOf<String>()
        
        // Validate all persons first
        personRequests.forEach { personRequest ->
            if (personRequest.mobileNumbers != null) {
                val hasPrimaryPhone = personRequest.mobileNumbers.any { it.isPrimary == true }
                require(hasPrimaryPhone) { 
                    "Person must have at least one primary phone number" 
                }
                
                personRequest.mobileNumbers
                    .filter { mobile -> mobile.isPrimary == true }
                    .forEach { phone ->
                        require(primaryPhones.add(phone.number ?: "")) { 
                            "Primary phone number ${phone.number} already exists" 
                        }
                    }
            }
        }
        
        // Create persons and return metadata
        return personRequests.map { personRequest ->
            val person = createNewPerson(personRequest)
            PersonData(
                personId = person.id!!,
                applicantType = personRequest.applicantType ?: "PRIMARY",
                relationshipToPrimary = personRequest.relationshipToPrimary ?: "SELF",
                tags = personRequest.tags ?: emptyList(),
                verificationStatus = personRequest.verificationStatus ?: "PENDING",
                verificationNotes = personRequest.verificationNotes ?: ""
            )
        }
    }


    private fun createNewPerson(personRequest: com.nivasafinance.features.lead.dto.PersonRequest): Person {
        val person = Person(
            firstName = personRequest.firstName,
            middleName = personRequest.middleName,
            lastName = personRequest.lastName,
            email = personRequest.email,
            dateOfBirth = personRequest.dateOfBirth?.let { LocalDate.parse(it) },
            gender = personRequest.gender,
            mobileNumbers = personRequest.mobileNumbers,
            extData = personRequest.extData
        )
        
        return personRepository.save(person)
    }


    private fun fetchPersonDetails(personData: PersonData, leadId: UUID): LeadPersonsResponse {
        // Fetch the actual person from database
        val person = personRepository.findById(personData.personId).orElse(null)
        
        return LeadPersonsResponse(
            leadId = leadId,
            personId = personData.personId,
            firstName = person?.firstName,
            middleName = person?.middleName,
            lastName = person?.lastName,
            email = person?.email,
            dateOfBirth = person?.dateOfBirth?.toString(),
            gender = person?.gender,
            mobileNumbers = person?.mobileNumbers,
            applicantType = personData.applicantType,
            relationshipToPrimary = personData.relationshipToPrimary,
            isPrimary = personData.tags.contains("PRIMARY"),
            tags = personData.tags,
            verificationStatus = personData.verificationStatus,
            verificationNotes = personData.verificationNotes,
            extData = person?.extData
        )
    }

    // New person management methods
    override fun addLeadPerson(leadId: UUID, addLeadPersonRequest: com.nivasafinance.features.lead.dto.AddLeadPersonRequest): LeadResponse {
        // Validate phone numbers
        validatePhoneNumbers(listOf(addLeadPersonRequest))
        
        // Create new person
        val person = Person(
            firstName = addLeadPersonRequest.firstName,
            middleName = addLeadPersonRequest.middleName,
            lastName = addLeadPersonRequest.lastName,
            email = addLeadPersonRequest.email,
            dateOfBirth = addLeadPersonRequest.dateOfBirth?.let { LocalDate.parse(it) },
            gender = addLeadPersonRequest.gender,
            mobileNumbers = addLeadPersonRequest.mobileNumbers,
            extData = addLeadPersonRequest.extData
        )
        
        val savedPerson = personRepository.save(person)
        
        // Create PersonData for the lead
        val newPersonData = PersonData(
            personId = savedPerson.id!!,
            applicantType = addLeadPersonRequest.applicantType ?: "PRIMARY",
            relationshipToPrimary = addLeadPersonRequest.relationshipToPrimary ?: "SELF",
            tags = addLeadPersonRequest.tags ?: emptyList(),
            verificationStatus = addLeadPersonRequest.verificationStatus ?: "PENDING",
            verificationNotes = addLeadPersonRequest.verificationNotes ?: ""
        )
        
        // Fetch fresh lead entity and update directly to avoid stale object exception
        val existingLead = leadRepositoryWrapper.findByIdWithException(leadId)
        
        // Add to existing person data by updating the field directly
        val currentPersonData = existingLead.personData ?: emptyList()
        existingLead.personData = currentPersonData + newPersonData
        
        val savedLead = leadRepositoryWrapper.saveWithException(existingLead)
        return toLeadResponse(savedLead)
    }

    override fun updateLeadPerson(leadId: UUID, personId: UUID, updateLeadPersonRequest: com.nivasafinance.features.lead.dto.UpdateLeadPersonRequest): LeadResponse {
        // Validate phone numbers
        validatePhoneNumbers(listOf(updateLeadPersonRequest))
        
        // Find and update the person
        val existingPerson = personRepository.findById(personId)
            .orElseThrow { IllegalArgumentException("Person with ID $personId not found") }
        
        // Update person fields
        existingPerson.firstName = updateLeadPersonRequest.firstName ?: existingPerson.firstName
        existingPerson.middleName = updateLeadPersonRequest.middleName ?: existingPerson.middleName
        existingPerson.lastName = updateLeadPersonRequest.lastName ?: existingPerson.lastName
        existingPerson.email = updateLeadPersonRequest.email ?: existingPerson.email
        existingPerson.dateOfBirth = updateLeadPersonRequest.dateOfBirth?.let { LocalDate.parse(it) } ?: existingPerson.dateOfBirth
        existingPerson.gender = updateLeadPersonRequest.gender ?: existingPerson.gender
        existingPerson.mobileNumbers = updateLeadPersonRequest.mobileNumbers ?: existingPerson.mobileNumbers
        existingPerson.extData = updateLeadPersonRequest.extData ?: existingPerson.extData
        
        personRepository.save(existingPerson)
        
        // Fetch fresh lead entity and update directly to avoid stale object exception
        val existingLead = leadRepositoryWrapper.findByIdWithException(leadId)
        
        // Update the PersonData in the lead by modifying the existing list
        val currentPersonData = existingLead.personData ?: emptyList()
        val updatedPersonData = currentPersonData.map { personData ->
            if (personData.personId == personId) {
                personData.copy(
                    applicantType = updateLeadPersonRequest.applicantType ?: personData.applicantType,
                    relationshipToPrimary = updateLeadPersonRequest.relationshipToPrimary ?: personData.relationshipToPrimary,
                    tags = updateLeadPersonRequest.tags ?: personData.tags,
                    verificationStatus = updateLeadPersonRequest.verificationStatus ?: personData.verificationStatus,
                    verificationNotes = updateLeadPersonRequest.verificationNotes ?: personData.verificationNotes
                )
            } else {
                personData
            }
        }
        
        existingLead.personData = updatedPersonData
        val savedLead = leadRepositoryWrapper.saveWithException(existingLead)
        return toLeadResponse(savedLead)
    }

    override fun getAllLeadPersons(paginationRequest: PaginationRequest): List<LeadPersonsResponse> {
        val pageable = org.springframework.data.domain.PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )
        val leadPage = leadRepositoryWrapper.findAllWithException(pageable)
        
        return leadPage.content.flatMap { lead ->
            lead.personData?.map { personData ->
                fetchPersonDetails(personData, lead.id!!)
            } ?: emptyList()
        }
    }

    private fun validatePhoneNumbers(personRequests: List<Any>) {
        val primaryPhones = mutableSetOf<String>()
        
        personRequests.forEach { personRequest ->
            val mobileNumbers = when (personRequest) {
                is com.nivasafinance.features.lead.dto.AddLeadPersonRequest -> personRequest.mobileNumbers
                is com.nivasafinance.features.lead.dto.UpdateLeadPersonRequest -> personRequest.mobileNumbers
                else -> null
            }
            
            if (mobileNumbers != null) {
                val hasPrimaryPhone = mobileNumbers.any { it.isPrimary == true }
                require(hasPrimaryPhone) { 
                    "Person must have at least one primary phone number" 
                }
                
                mobileNumbers
                    .filter { mobile -> mobile.isPrimary == true }
                    .forEach { phone ->
                        require(primaryPhones.add(phone.number ?: "")) { 
                            "Primary phone number ${phone.number} already exists" 
                        }
                    }
            }
        }
    }
}
