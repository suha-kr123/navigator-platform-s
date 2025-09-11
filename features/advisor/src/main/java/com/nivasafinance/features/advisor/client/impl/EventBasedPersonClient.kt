package com.nivasafinance.features.advisor.client.impl

import client.PersonClient
import event.EmailAddress
import event.EventTopics
import event.GetPersonByMobileRequest
import event.GetPersonRequest
import event.MobileNumber
import event.PersonInfo
import event.impl.SpringEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Component
@Suppress("VarCouldBeVal")
class EventBasedPersonClient : PersonClient {

    @Autowired
    private lateinit var eventService: SpringEventService

    override fun getPerson(id: UUID): CompletableFuture<PersonInfo> {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = GetPersonRequest(
            requestId = requestId,
            personId = id,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.PERSON_GET_REQUEST)

        // In a real implementation, you'd wait for the response event
        // For now, we'll simulate a successful response
        val personInfo = PersonInfo(
            id = id,
            firstName = "John",
            lastName = "Doe",
            mobileNumbers = listOf(
                MobileNumber(
                    number = "9876543210",
                    isPrimary = true,
                    isVerified = true
                )
            ),
            emailAddresses = listOf(
                EmailAddress(
                    email = "john.doe@example.com",
                    isPrimary = true,
                    isVerified = true
                )
            ),
            dateOfBirth = "1990-01-01",
            gender = "MALE",
            status = "ACTIVE"
        )

        return CompletableFuture.completedFuture(personInfo)
    }

    override fun getPersonByMobile(mobileNumber: String): CompletableFuture<PersonInfo?> {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = GetPersonByMobileRequest(
            requestId = requestId,
            mobileNumber = mobileNumber,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.PERSON_GET_BY_MOBILE_REQUEST)

        // In a real implementation, you'd wait for the response event
        // For now, we'll simulate a successful response
        val personInfo = PersonInfo(
            id = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            mobileNumbers = listOf(
                MobileNumber(
                    number = mobileNumber,
                    isPrimary = true,
                    isVerified = true
                )
            ),
            emailAddresses = listOf(
                EmailAddress(
                    email = "john.doe@example.com",
                    isPrimary = true,
                    isVerified = true
                )
            ),
            dateOfBirth = "1990-01-01",
            gender = "MALE",
            status = "ACTIVE"
        )

        return CompletableFuture.completedFuture(personInfo)
    }
}
