package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.dto.PersonIdentifierUpdateRequest
import com.nivasafinance.features.person.enum.IdentifierType
import com.nivasafinance.features.person.service.PersonService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonIdentifierController Tests")
class PersonIdentifierControllerTest {

    private val personService = mockk<PersonService>()
    private lateinit var personIdentifierController: PersonIdentifierController

    private val personId = UUID.randomUUID()
    private val identifierId = UUID.randomUUID()
    private val expectedResponse = PersonIdentifierResponse(
        id = identifierId,
        personId = personId,
        identifier = "123456789012",
        type = IdentifierType.AADHAR
    )

    @BeforeEach
    fun setup() {
        personIdentifierController = PersonIdentifierController(personService)
    }

    @Test
    @DisplayName("should get person identifiers successfully")
    fun `getPersonIdentifiers should get person identifiers successfully`() {
        val identifiers = listOf(expectedResponse)
        every { personService.getPersonIdentifiers(personId) } returns identifiers

        val result = personIdentifierController.getPersonIdentifiers(personId)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(identifiers, result.body)
        verify(exactly = 1) { personService.getPersonIdentifiers(personId) }
    }

    @Test
    @DisplayName("should create person identifier successfully")
    fun `createPersonIdentifier should create person identifier successfully`() {
        val request = PersonIdentifierCreateRequest(
            identifier = "123456789012",
            type = IdentifierType.AADHAR
        )
        every { personService.createPersonIdentifier(personId, request) } returns expectedResponse

        val result = personIdentifierController.createPersonIdentifier(personId, request)

        assertNotNull(result)
        assertEquals(201, result.statusCode.value())
        assertEquals(expectedResponse, result.body)
        verify(exactly = 1) { personService.createPersonIdentifier(personId, request) }
    }

    @Test
    @DisplayName("should update person identifier successfully")
    fun `updatePersonIdentifier should update person identifier successfully`() {
        val request = PersonIdentifierUpdateRequest(
            identifier = "987654321098",
            type = null
        )
        every { personService.updatePersonIdentifier(personId, identifierId, request) } returns expectedResponse

        val result = personIdentifierController.updatePersonIdentifier(personId, identifierId, request)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedResponse, result.body)
        verify(exactly = 1) { personService.updatePersonIdentifier(personId, identifierId, request) }
    }

    @Test
    @DisplayName("should delete person identifier successfully")
    fun `deletePersonIdentifier should delete person identifier successfully`() {
        every { personService.deletePersonIdentifier(personId, identifierId) } returns Unit

        val result = personIdentifierController.deletePersonIdentifier(personId, identifierId)

        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { personService.deletePersonIdentifier(personId, identifierId) }
    }
}
