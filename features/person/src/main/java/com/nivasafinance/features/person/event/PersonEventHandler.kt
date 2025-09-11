package com.nivasafinance.features.person.event

import com.nivasafinance.features.person.service.PersonService
import event.*
import event.impl.SpringEventService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PersonEventHandler(
    private val personService: PersonService,
    private val eventService: SpringEventService
) {

    @KafkaListener(topics = [EventTopics.PERSON_GET_REQUEST])
    fun handleGetPersonRequest(request: GetPersonRequest) {
        try {
            val person = personService.getPerson(request.personId)
            val response = GetPersonResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = true,
                personInfo = PersonInfo(
                    id = person.id!!,
                    firstName = person.firstName ?: "",
                    lastName = person.lastName ?: "",
                    mobileNumbers = person.mobileNumbers?.map {
                        MobileNumber(
                            number = it.number ?: "",
                            isPrimary = it.isPrimary ?: false,
                            isVerified = true // Default to true for now
                        )
                    } ?: emptyList(),
                    emailAddresses = listOf(
                        EmailAddress(
                            email = person.email ?: "",
                            isPrimary = true,
                            isVerified = true
                        )
                    ),
                    dateOfBirth = person.dateOfBirth?.toString(),
                    gender = person.gender?.name,
                    status = "ACTIVE"
                )
            )
            eventService.publishEvent(response, EventTopics.PERSON_GET_RESPONSE)
        } catch (e: Exception) {
            val response = GetPersonResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = false,
                error = e.message
            )
            eventService.publishEvent(response, EventTopics.PERSON_GET_RESPONSE)
        }
    }

    @KafkaListener(topics = [EventTopics.PERSON_GET_BY_MOBILE_REQUEST])
    fun handleGetPersonByMobileRequest(request: GetPersonByMobileRequest) {
        try {
            val person = personService.getPersonByMobileNo(request.mobileNumber)
            val response = GetPersonByMobileResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = true,
                personInfo = person?.let {
                    PersonInfo(
                        id = it.id!!,
                        firstName = it.firstName ?: "",
                        lastName = it.lastName ?: "",
                        mobileNumbers = it.mobileNumbers?.map { mobileNumber ->
                            MobileNumber(
                                number = mobileNumber.number ?: "",
                                isPrimary = mobileNumber.isPrimary ?: false,
                                isVerified = true
                            )
                        } ?: emptyList(),
                        emailAddresses = listOf(
                            EmailAddress(
                                email = it.email ?: "",
                                isPrimary = true,
                                isVerified = true
                            )
                        ),
                        dateOfBirth = it.dateOfBirth?.toString(),
                        gender = it.gender?.name,
                        status = "ACTIVE"
                    )
                }
            )
            eventService.publishEvent(response, EventTopics.PERSON_GET_BY_MOBILE_RESPONSE)
        } catch (e: Exception) {
            val response = GetPersonByMobileResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = false,
                error = e.message
            )
            eventService.publishEvent(response, EventTopics.PERSON_GET_BY_MOBILE_RESPONSE)
        }
    }
}
