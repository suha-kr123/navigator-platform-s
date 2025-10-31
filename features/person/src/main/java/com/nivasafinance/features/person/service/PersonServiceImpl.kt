package com.nivasafinance.features.person.service

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationInfo
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonExceptionFactory
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class PersonServiceImpl(
    private val personRepositoryWrapper: PersonRepositoryWrapper,
    private val messageSource: MessageSource
) : PersonService {

    private fun validatePrimaryMobileNumber(mobileNumbers: List<MobileNumberDetails>?, excludePersonId: UUID? = null) {
        mobileNumbers?.forEach { mobileNumber ->
            if (mobileNumber.isPrimary == true && mobileNumber.number != null) {
                val number = checkNotNull(mobileNumber.number)
                val existingPersons = if (excludePersonId != null) {
                    personRepositoryWrapper.findByMobileNumberExcludingIdWithException(
                        number,
                        excludePersonId
                    )
                } else {
                    personRepositoryWrapper.findByMobileNumberWithException(number)
                }

                if (existingPersons.isNotEmpty()) {
                    throw PersonExceptionFactory.primaryMobileAlreadyExists(number, messageSource)
                }
            }
        }
    }

    override fun createPerson(personRequest: PersonCreateRequest): PersonResponse {
        // Validate primary mobile number uniqueness
        validatePrimaryMobileNumber(personRequest.mobileNumbers)

        val person = Person(
            firstName = personRequest.firstName,
            middleName = personRequest.middleName,
            lastName = personRequest.lastName,
            mobileNumbers = personRequest.mobileNumbers,
            dateOfBirth = personRequest.dateOfBirth,
            gender = personRequest.gender,
            extData = personRequest.extData
        )

        val savedPerson = personRepositoryWrapper.saveWithException(person)
        return toPersonResponse(savedPerson)
    }

    override fun updatePerson(personId: UUID, personUpdateRequest: PersonUpdateRequest): PersonResponse {
        val existingPerson = personRepositoryWrapper.findByIdWithException(personId)

        // Validate primary mobile number uniqueness (excluding current person) if mobile numbers are being updated
        personUpdateRequest.mobileNumbers?.let {
            validatePrimaryMobileNumber(it, personId)
        }

        // Update fields directly (only if provided)
        personUpdateRequest.firstName?.let { existingPerson.firstName = it }
        personUpdateRequest.middleName?.let { existingPerson.middleName = it }
        personUpdateRequest.lastName?.let { existingPerson.lastName = it }
        personUpdateRequest.mobileNumbers?.let { existingPerson.mobileNumbers = it }
        personUpdateRequest.dateOfBirth?.let { existingPerson.dateOfBirth = it }
        personUpdateRequest.gender?.let { existingPerson.gender = it }
        personUpdateRequest.extData?.let { existingPerson.extData = it }

        val savedPerson = personRepositoryWrapper.saveWithException(existingPerson)
        return toPersonResponse(savedPerson)
    }

    override fun getPerson(personId: UUID): PersonResponse {
        val person = personRepositoryWrapper.findByIdWithException(personId)
        return toPersonResponse(person)
    }

    override fun getAllPersons(paginationRequest: PaginationRequest): PaginatedResponse<PersonResponse> {
        val sort = if (paginationRequest.sortBy != null) {
            Sort.by(
                if (paginationRequest.sortDirection == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
                paginationRequest.sortBy
            )
        } else {
            Sort.by(Sort.Direction.DESC, "createdAt")
        }

        val pageable = PageRequest.of(paginationRequest.offset / paginationRequest.limit, paginationRequest.limit, sort)
        val personsPage = personRepositoryWrapper.findAllWithException(pageable)

        return PaginatedResponse(
            personsPage.content.map { toPersonResponse(it) },
            PaginationInfo(
                paginationRequest.offset,
                paginationRequest.limit,
                personsPage.totalElements,
                personsPage.totalPages,
                personsPage.number,
                personsPage.hasNext(),
                personsPage.hasPrevious()
            )
        )
    }

    private fun toPersonResponse(person: Person): PersonResponse {
        return PersonResponse(
            id = checkNotNull(person.id) { "Person ID cannot be null" },
            firstName = person.firstName,
            middleName = person.middleName,
            lastName = person.lastName,
            mobileNumbers = person.mobileNumbers,
            dateOfBirth = person.dateOfBirth,
            gender = person.gender?.name,
            extData = person.extData,
            createdAt = person.createdAt ?: java.time.LocalDateTime.now(),
            createdBy = person.createdBy,
            updatedAt = person.updatedAt ?: java.time.LocalDateTime.now(),
            updatedBy = person.updatedBy
        )
    }
}
