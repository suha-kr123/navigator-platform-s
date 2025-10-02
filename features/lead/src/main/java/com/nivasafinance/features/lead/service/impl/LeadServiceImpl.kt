package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.AddLeadPersonRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadPersonsResponse
import com.nivasafinance.features.lead.dto.LeadPreliminaryInformation
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.dto.PersonRequest
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.PersonData
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.stages.service.StageService
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
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
                leadCreateRequest.requestedAmountRange,
                leadCreateRequest.purpose,
                leadCreateRequest.productCode,
                messageSource
        )

        val personData =
                leadCreateRequest.leadPersons?.let { leadPersons ->
                    validateAndCreatePersons(leadPersons)
                }
                        ?: emptyList()

        val lead = toLead(leadCreateRequest, personData)
        val savedLead = leadRepositoryWrapper.saveWithException(lead)

        val stages = createStagesForLead("HOME_LOAN")

        val stageIds = stages.map { it.id }
        val updatedLead = savedLead.copy(stageIds = stageIds)
        val finalLead = leadRepositoryWrapper.saveWithException(updatedLead)

        return toLeadResponse(finalLead)
    }

    override fun updateLead(id: UUID, leadUpdateRequest: LeadUpdateRequest): LeadResponse {
        val existingLead = leadRepositoryWrapper.findByIdWithException(id)

        val updatedLead =
                existingLead.copy(
                        requestedAmountRange = leadUpdateRequest.requestedAmountRange
                                        ?: existingLead.requestedAmountRange,
                        purpose = leadUpdateRequest.purpose ?: existingLead.purpose,
                        productCode = leadUpdateRequest.productCode ?: existingLead.productCode,
                        pipelineKey = leadUpdateRequest.pipelineKey ?: existingLead.pipelineKey,
                        sourcingChannel = leadUpdateRequest.sourcingChannel
                                        ?: existingLead.sourcingChannel,
                        preliminaryInformation =
                                if (leadUpdateRequest.preliminaryInformation != null) {
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

    override fun getAllLeads(
            paginationRequest: PaginationRequest,
            search: String?
    ): PaginatedResponse<LeadResponse> {
        val pageable =
                PageRequest.of(
                        paginationRequest.offset / paginationRequest.limit,
                        paginationRequest.limit
                )
        
        val leadPage = if (search.isNullOrBlank()) {
            leadRepositoryWrapper.findAllWithException(pageable)
        } else {
            leadRepositoryWrapper.findByPersonNameOrPhoneNumberWithException(search, pageable)
        }
        
        val leadResponses = leadPage.content.map { toLeadResponse(it) }

        val totalPages =
                if (leadPage.totalElements == 0L) 0
                else ((leadPage.totalElements - 1) / paginationRequest.limit + 1).toInt()
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
                content = leadResponses,
                pagination =
                        PaginationInfo(
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
                requestedAmountRange = leadCreateRequest.requestedAmountRange,
                purpose = leadCreateRequest.purpose,
                productCode = leadCreateRequest.productCode,
                pipelineKey = "HOME_LOAN",
                currentStage = "APPLICATION_RECEIVED",
                sourcingChannel = leadCreateRequest.sourcingChannel,
                preliminaryInformation =
                        leadCreateRequest.preliminaryInformation?.let { mapOf("data" to it) },
                extData = leadCreateRequest.extData,
                personData = personData
        )
    }

    private fun toLeadResponse(lead: Lead): LeadResponse {
        return LeadResponse(
                id = lead.id ?: UUID.randomUUID(),
                requestedAmountRange = lead.requestedAmountRange,
                purpose = lead.purpose,
                productCode = lead.productCode,
                currentStage = lead.currentStage,
                preliminaryInformation =
                        lead.preliminaryInformation?.get("data") as? LeadPreliminaryInformation,
                sourcingChannel = lead.sourcingChannel,
                extData = lead.extData,
                taskData =
                        lead.taskData?.let { taskDataList ->
                            taskDataList.associate { taskData ->
                                taskData.taskId to
                                        mapOf(
                                                "notesIds" to taskData.notesIds,
                                                "callIds" to taskData.callIds
                                        )
                            }
                        },
                createdAt = lead.createdAt ?: java.time.LocalDateTime.now(),
                createdBy = lead.createdBy,
                updatedAt = lead.updatedAt ?: java.time.LocalDateTime.now(),
                updatedBy = lead.updatedBy,
                leadPersons =
                        lead.personData?.map { personData ->
                            fetchPersonDetails(personData, lead.id!!)
                        },

            )
    }

    private fun createStagesForLead(
            pipelineKey: String?
    ): List<com.nivasafinance.features.stages.dto.StageResponse> {
        if (pipelineKey.isNullOrBlank()) {
            return emptyList()
        }

        val stageDefinitions =
                stageDefinitionRepositoryWrapper.findByPipelineKeyWithException(pipelineKey)
        val stages = mutableListOf<com.nivasafinance.features.stages.dto.StageResponse>()

        stageDefinitions.forEach { stageDefinition ->
            val validOutcome = stageDefinition.possibleOutcomes?.firstOrNull() ?: "PENDING"
            val stageRequest =
                    StageRequest(
                            stageDefinitionKey = stageDefinition.key,
                            outcome = validOutcome,
                            assignedTo = null
                    )
            val savedStage = stageService.createStage(stageRequest)
            stages.add(savedStage)
        }

        return stages
    }

    private fun validateAndCreatePersons(personRequests: List<PersonRequest>): List<PersonData> {
        val primaryPhones = mutableSetOf<String>()

        personRequests.forEach { personRequest ->
            personRequest.mobileNumbers?.let { mobileNumbers ->
                LeadExceptionFactory.validatePersonForCreation(mobileNumbers, messageSource)

                mobileNumbers.filter { mobile -> mobile.isPrimary == true }.forEach { phone ->
                    LeadExceptionFactory.validatePhoneNumberUniqueness(
                            phone.number,
                            primaryPhones,
                            messageSource
                    )
                }
            }
        }

        return personRequests.map { personRequest ->
            val person = createNewPerson(personRequest)
            PersonData(
                    personId = person.id!!,
                    applicantType = personRequest.applicantType ?: "PRIMARY",
                    relationshipToPrimary = personRequest.relationshipToPrimary ?: "SELF"
            )
        }
    }

    private fun createNewPerson(personRequest: PersonRequest): Person {
        val person =
                Person(
                        firstName = personRequest.firstName,
                        middleName = personRequest.middleName,
                        lastName = personRequest.lastName,
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
                dateOfBirth = person?.dateOfBirth?.toString(),
                gender = person?.gender,
                mobileNumbers = person?.mobileNumbers,
                applicantType = personData.applicantType,
                relationshipToPrimary = personData.relationshipToPrimary,
                extData = person?.extData
        )
    }

    override fun addLeadPerson(
            leadId: UUID,
            addLeadPersonRequest: AddLeadPersonRequest
    ): LeadResponse {
        validatePhoneNumbers(listOf(addLeadPersonRequest))

        val person =
                Person(
                        firstName = addLeadPersonRequest.firstName,
                        middleName = addLeadPersonRequest.middleName,
                        lastName = addLeadPersonRequest.lastName,
                        dateOfBirth = addLeadPersonRequest.dateOfBirth?.let { LocalDate.parse(it) },
                        gender = addLeadPersonRequest.gender,
                        mobileNumbers = addLeadPersonRequest.mobileNumbers,
                        extData = addLeadPersonRequest.extData
                )

        val savedPerson = personRepository.save(person)

        val newPersonData =
                PersonData(
                        personId = savedPerson.id!!,
                        applicantType = addLeadPersonRequest.applicantType ?: "PRIMARY",
                        relationshipToPrimary = addLeadPersonRequest.relationshipToPrimary ?: "SELF"
                )

        val existingLead = leadRepositoryWrapper.findByIdWithException(leadId)

        val currentPersonData = existingLead.personData ?: emptyList()
        existingLead.personData = currentPersonData + newPersonData

        val savedLead = leadRepositoryWrapper.saveWithException(existingLead)
        return toLeadResponse(savedLead)
    }

    override fun updateLeadPerson(
            leadId: UUID,
            personId: UUID,
            updateLeadPersonRequest: com.nivasafinance.features.lead.dto.UpdateLeadPersonRequest
    ): LeadResponse {
        validatePhoneNumbers(listOf(updateLeadPersonRequest))

        val existingPerson =
                personRepository.findById(personId).orElseThrow {
                    LeadExceptionFactory.personNotFound(personId, messageSource)
                }

        existingPerson.firstName = updateLeadPersonRequest.firstName ?: existingPerson.firstName
        existingPerson.middleName = updateLeadPersonRequest.middleName ?: existingPerson.middleName
        existingPerson.lastName = updateLeadPersonRequest.lastName ?: existingPerson.lastName
        existingPerson.dateOfBirth =
                updateLeadPersonRequest.dateOfBirth?.let { LocalDate.parse(it) }
                        ?: existingPerson.dateOfBirth
        existingPerson.gender = updateLeadPersonRequest.gender ?: existingPerson.gender
        existingPerson.mobileNumbers =
                updateLeadPersonRequest.mobileNumbers ?: existingPerson.mobileNumbers
        existingPerson.extData = updateLeadPersonRequest.extData ?: existingPerson.extData

        personRepository.save(existingPerson)

        val existingLead = leadRepositoryWrapper.findByIdWithException(leadId)

        val currentPersonData = existingLead.personData ?: emptyList()
        val updatedPersonData =
                currentPersonData.map { personData ->
                    if (personData.personId == personId) {
                        personData.copy(
                                applicantType = updateLeadPersonRequest.applicantType
                                                ?: personData.applicantType,
                                relationshipToPrimary =
                                        updateLeadPersonRequest.relationshipToPrimary
                                                ?: personData.relationshipToPrimary
                        )
                    } else {
                        personData
                    }
                }

        existingLead.personData = updatedPersonData
        val savedLead = leadRepositoryWrapper.saveWithException(existingLead)
        return toLeadResponse(savedLead)
    }

    override fun getAllLeadPersons(
            paginationRequest: PaginationRequest,
            search: String?
    ): List<LeadPersonsResponse> {
        val pageable =
                PageRequest.of(
                        paginationRequest.offset / paginationRequest.limit,
                        paginationRequest.limit
                )
        
        val leadPage = if (search.isNullOrBlank()) {
            leadRepositoryWrapper.findAllWithException(pageable)
        } else {
            leadRepositoryWrapper.findByPersonNameOrPhoneNumberWithException(search, pageable)
        }

        return leadPage.content.flatMap { lead ->
            lead.personData?.map { personData -> fetchPersonDetails(personData, lead.id!!) }
                    ?: emptyList()
        }
    }

    private fun validatePhoneNumbers(personRequests: List<Any>) {
        val primaryPhones = mutableSetOf<String>()

        personRequests.forEach { personRequest ->
            val mobileNumbers =
                    when (personRequest) {
                        is AddLeadPersonRequest -> personRequest.mobileNumbers
                        is com.nivasafinance.features.lead.dto.UpdateLeadPersonRequest ->
                                personRequest.mobileNumbers
                        else -> null
                    }

            mobileNumbers?.let { numbers ->
                LeadExceptionFactory.validatePersonForCreation(numbers, messageSource)

                numbers.filter { mobile -> mobile.isPrimary == true }.forEach { phone ->
                    LeadExceptionFactory.validatePhoneNumberUniqueness(
                            phone.number,
                            primaryPhones,
                            messageSource
                    )
                }
            }
        }
    }
}
