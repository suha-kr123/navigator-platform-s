package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsCreateRequest
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsResponse
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsCreateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.service.PersonService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonEmploymentController Tests")
class PersonEmploymentControllerTest {

    private val personService = mockk<PersonService>()
    private lateinit var personEmploymentController: PersonEmploymentController

    private val personId = UUID.randomUUID()
    private val employmentId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        personEmploymentController = PersonEmploymentController(personService)
    }

    @Test
    @DisplayName("should create employment details successfully")
    fun `createEmploymentDetails should create employment details successfully`() {
        // Given
        val createRequest = createTestEmploymentDetailsCreateRequest()
        val expectedResponse = createTestEmploymentDetailsResponse(
            employmentId = employmentId,
            personId = personId
        )

        every { personService.createPersonEmploymentDetails(personId, createRequest) } returns expectedResponse

        // When
        val result = personEmploymentController.createEmploymentDetails(personId, createRequest)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertNotNull(result.body)
        assertEquals(expectedResponse.employmentId, result.body?.employmentId)
        assertEquals(expectedResponse.personId, result.body?.personId)
        assertEquals(expectedResponse.employerName, result.body?.employerName)
        assertEquals(expectedResponse.employerType, result.body?.employerType)
        assertEquals(expectedResponse.jobTitle, result.body?.jobTitle)
        assertEquals(expectedResponse.department, result.body?.department)
        assertEquals(expectedResponse.employmentType, result.body?.employmentType)
        assertEquals(expectedResponse.location, result.body?.location)
        assertEquals(expectedResponse.salary, result.body?.salary)
        assertEquals(expectedResponse.documents, result.body?.documents)
        assertEquals(expectedResponse.extData, result.body?.extData)

        verify { personService.createPersonEmploymentDetails(personId, createRequest) }
    }

    @Test
    @DisplayName("should get employment details successfully")
    fun `getEmploymentDetails should get employment details successfully`() {
        // Given
        val expectedResponse = createTestEmploymentDetailsResponse(
            employmentId = employmentId,
            personId = personId
        )

        every { personService.getPersonEmploymentDetails(personId) } returns expectedResponse

        // When
        val result = personEmploymentController.getEmploymentDetails(personId)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertNotNull(result.body)
        assertEquals(expectedResponse.employmentId, result.body?.employmentId)
        assertEquals(expectedResponse.personId, result.body?.personId)
        assertEquals(expectedResponse.employerName, result.body?.employerName)

        verify { personService.getPersonEmploymentDetails(personId) }
    }

    @Test
    @DisplayName("should return null when employment details not found")
    fun `getEmploymentDetails should return null when employment details not found`() {
        // Given
        every { personService.getPersonEmploymentDetails(personId) } returns null

        // When
        val result = personEmploymentController.getEmploymentDetails(personId)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(null, result.body)

        verify { personService.getPersonEmploymentDetails(personId) }
    }

    @Test
    @DisplayName("should update employment details successfully")
    fun `updateEmploymentDetails should update employment details successfully`() {
        // Given
        val updateRequest = createTestEmploymentDetailsUpdateRequest()
        val expectedResponse = createTestEmploymentDetailsResponse(
            employmentId = employmentId,
            personId = personId,
            employerName = "Updated Company"
        )

        every { personService.updatePersonEmploymentDetails(personId, updateRequest) } returns expectedResponse

        // When
        val result = personEmploymentController.updateEmploymentDetails(personId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertNotNull(result.body)
        assertEquals(expectedResponse.employmentId, result.body?.employmentId)
        assertEquals(expectedResponse.personId, result.body?.personId)
        assertEquals(expectedResponse.employerName, result.body?.employerName)

        verify { personService.updatePersonEmploymentDetails(personId, updateRequest) }
    }

    @Test
    @DisplayName("should delete employment details successfully")
    fun `deleteEmploymentDetails should delete employment details successfully`() {
        // Given
        every { personService.deletePersonEmploymentDetails(personId) } returns Unit

        // When
        val result = personEmploymentController.deleteEmploymentDetails(personId)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)

        verify { personService.deletePersonEmploymentDetails(personId) }
    }

    @Test
    @DisplayName("should handle partial update with null values")
    fun `updateEmploymentDetails should handle partial update with null values`() {
        // Given
        val updateRequest = EmploymentDetailsUpdateRequest(
            employerName = "Updated Company",
            employerType = null,
            jobTitle = null,
            department = null,
            employmentType = null,
            location = null,
            salary = null,
            documents = null,
            extData = null
        )
        val expectedResponse = createTestEmploymentDetailsResponse(
            employmentId = employmentId,
            personId = personId,
            employerName = "Updated Company"
        )

        every { personService.updatePersonEmploymentDetails(personId, updateRequest) } returns expectedResponse

        // When
        val result = personEmploymentController.updateEmploymentDetails(personId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertNotNull(result.body)
        assertEquals(expectedResponse.employerName, result.body?.employerName)

        verify { personService.updatePersonEmploymentDetails(personId, updateRequest) }
    }

    @Test
    @DisplayName("should handle create request with minimal data")
    fun `createEmploymentDetails should handle create request with minimal data`() {
        // Given
        val createRequest = EmploymentDetailsCreateRequest(
            employerName = "Minimal Company",
            employerType = null,
            jobTitle = null,
            department = null,
            employmentType = null,
            location = null,
            salary = null,
            documents = null,
            extData = null
        )
        val expectedResponse = createTestEmploymentDetailsResponse(
            employmentId = employmentId,
            personId = personId,
            employerName = "Minimal Company"
        )

        every { personService.createPersonEmploymentDetails(personId, createRequest) } returns expectedResponse

        // When
        val result = personEmploymentController.createEmploymentDetails(personId, createRequest)

        // Then
        assertNotNull(result)
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertNotNull(result.body)
        assertEquals(expectedResponse.employerName, result.body?.employerName)

        verify { personService.createPersonEmploymentDetails(personId, createRequest) }
    }
}
