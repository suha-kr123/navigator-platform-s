package com.nivasafinance.features.person.controller

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

@DisplayName("PersonController Tests")
class PersonControllerTest {

    private val personService = mockk<PersonService>()
    private lateinit var personController: PersonController

    private val personId = UUID.randomUUID()
    private val expectedResponse = com.nivasafinance.features.person.dto.PersonResponse(
        id = personId,
        firstName = "John",
        lastName = "Doe",
        mobileNumbers = emptyList(),
        email = "john.doe@example.com",
        gender = data.enums.Gender.MALE
    )

    @BeforeEach
    fun setup() {
        personController = PersonController(personService)
    }

    @Test
    @DisplayName("should return person when found")
    fun `getPerson should return person when found`() {
        // Given
        every { personService.getPerson(personId) } returns expectedResponse

        // When
        val result = personController.getPerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.firstName, result.body?.firstName)
        assertEquals(expectedResponse.lastName, result.body?.lastName)

        verify(exactly = 1) { personService.getPerson(personId) }
    }

    @Test
    @DisplayName("should create person successfully")
    fun `createPerson should create person successfully`() {
        // Given
        val newPersonRequest = com.nivasafinance.features.person.dto.PersonCreateRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            gender = data.enums.Gender.MALE
        )
        every { personService.createPerson(newPersonRequest) } returns expectedResponse

        // When
        val result = personController.createPerson(newPersonRequest)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.firstName, result.body?.firstName)
        assertEquals(expectedResponse.lastName, result.body?.lastName)

        verify(exactly = 1) { personService.createPerson(newPersonRequest) }
    }

    @Test
    @DisplayName("should update person successfully")
    fun `updatePerson should update person successfully`() {
        // Given
        val updateRequest = com.nivasafinance.features.person.dto.PersonUpdateRequest(
            firstName = "Jane",
            lastName = "Doe"
        )
        every { personService.updatePerson(personId, updateRequest) } returns expectedResponse

        // When
        val result = personController.updatePerson(personId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.firstName, result.body?.firstName)
        assertEquals(expectedResponse.lastName, result.body?.lastName)

        verify(exactly = 1) { personService.updatePerson(personId, updateRequest) }
    }

    @Test
    @DisplayName("should delete person successfully")
    fun `deletePerson should delete person successfully`() {
        // Given
        every { personService.deletePerson(personId) } returns Unit

        // When
        val result = personController.deletePerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { personService.deletePerson(personId) }
    }
}
