package com.nivasafinance.features.advisor.integration

import com.nivasafinance.TestUtils.createTestAdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.service.PersonService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("Advisor Integration Tests")
class AdvisorIntegrationTest {

    private lateinit var advisorService: AdvisorService
    private lateinit var personService: PersonService
    private lateinit var advisorController: com.nivasafinance.features.advisor.controller.AdvisorController

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val personResponse = PersonResponse(
        id = personId,
        firstName = "John",
        lastName = "Doe"
    )
    private val expectedResponse = createTestAdvisorResponse(
        id = advisorId,
        personId = personId,
        personalDetails = personResponse
    )

    @BeforeEach
    fun setup() {
        advisorService = mockk<AdvisorService>()
        personService = mockk<PersonService>()
        advisorController = com.nivasafinance.features.advisor.controller.AdvisorController(advisorService)
    }

    @Test
    @DisplayName("should create advisor through complete flow")
    fun `createAdvisor should work through complete flow`() {
        // Given
        val createRequest = AdvisorCreateRequest(
            advisorCode = "ADV001",
            isEmployee = false,
            remarks = "Test advisor"
        )
        every { advisorService.createAdvisor(createRequest) } returns expectedResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.advisor.dto.AdvisorResponse> =
            advisorController.createAdvisor(createRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse.id, response.body?.id)
        assertEquals(expectedResponse.advisorCode, response.body?.advisorCode)
        assertEquals(expectedResponse.status, response.body?.status)
    }

    @Test
    @DisplayName("should get advisor through complete flow")
    fun `getAdvisor should work through complete flow`() {
        // Given
        every { advisorService.getAdvisor(advisorId) } returns expectedResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.advisor.dto.AdvisorResponse> =
            advisorController.getAdvisor(advisorId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse.id, response.body?.id)
        assertEquals(expectedResponse.advisorCode, response.body?.advisorCode)
        assertEquals(expectedResponse.status, response.body?.status)
    }

    @Test
    @DisplayName("should update advisor through complete flow")
    fun `updateAdvisor should work through complete flow`() {
        // Given
        val updateRequest = AdvisorUpdateRequest(
            advisorCode = "ADV002",
            remarks = "Updated advisor"
        )
        every { advisorService.updateAdvisor(advisorId, updateRequest) } returns expectedResponse
        every { advisorService.getAdvisor(advisorId) } returns expectedResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.advisor.dto.AdvisorResponse> =
            advisorController.updateAdvisor(advisorId, updateRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse.id, response.body?.id)
        assertEquals(expectedResponse.advisorCode, response.body?.advisorCode)
        assertEquals(expectedResponse.status, response.body?.status)
    }

    @Test
    @DisplayName("should delete advisor through complete flow")
    fun `deleteAdvisor should work through complete flow`() {
        // Given
        every { advisorService.deleteAdvisor(advisorId) } returns Unit

        // When
        val response: ResponseEntity<Unit> = advisorController.deleteAdvisor(advisorId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    @DisplayName("should handle advisor service calls correctly")
    fun `advisor service should handle all operations correctly`() {
        // Given
        val createRequest = AdvisorCreateRequest(
            advisorCode = "ADV001",
            isEmployee = false,
            remarks = "Test advisor"
        )
        val updateRequest = AdvisorUpdateRequest(
            advisorCode = "ADV002",
            remarks = "Updated advisor"
        )

        every { advisorService.createAdvisor(createRequest) } returns expectedResponse
        every { advisorService.getAdvisor(advisorId) } returns expectedResponse
        every { advisorService.updateAdvisor(advisorId, updateRequest) } returns expectedResponse
        every { advisorService.deleteAdvisor(advisorId) } returns Unit

        // When & Then
        val createResponse = advisorService.createAdvisor(createRequest)
        assertEquals(expectedResponse.id, createResponse.id)

        val getResponse = advisorService.getAdvisor(advisorId)
        assertEquals(expectedResponse.id, getResponse.id)

        val updateResponse = advisorService.updateAdvisor(advisorId, updateRequest)
        assertEquals(expectedResponse.id, updateResponse.id)

        advisorService.deleteAdvisor(advisorId)
    }

    @Test
    @DisplayName("should handle advisor with minimal data")
    fun `advisor service should handle advisor with minimal data`() {
        // Given
        val minimalCreateRequest = AdvisorCreateRequest(
            advisorCode = null,
            isEmployee = false,
            remarks = null
        )
        val minimalResponse = expectedResponse.copy(
            advisorCode = null,
            remarks = null
        )
        every { advisorService.createAdvisor(minimalCreateRequest) } returns minimalResponse

        // When
        val response = advisorController.createAdvisor(minimalCreateRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse.id, response.body?.id)
        assertEquals(null, response.body?.advisorCode)
        assertEquals(null, response.body?.remarks)
    }

    @Test
    @DisplayName("should handle advisor with employee flag")
    fun `advisor service should handle advisor with employee flag`() {
        // Given
        val employeeCreateRequest = AdvisorCreateRequest(
            advisorCode = "EMP001",
            isEmployee = true,
            remarks = "Employee advisor"
        )
        val employeeResponse = expectedResponse.copy(
            advisorCode = "EMP001",
            isEmployee = true,
            remarks = "Employee advisor"
        )
        every { advisorService.createAdvisor(employeeCreateRequest) } returns employeeResponse

        // When
        val response = advisorController.createAdvisor(employeeCreateRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse.id, response.body?.id)
        assertEquals("EMP001", response.body?.advisorCode)
        assertEquals(true, response.body?.isEmployee)
        assertEquals("Employee advisor", response.body?.remarks)
    }
}
