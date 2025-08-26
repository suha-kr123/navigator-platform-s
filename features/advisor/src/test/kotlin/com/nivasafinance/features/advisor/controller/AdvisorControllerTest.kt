package com.nivasafinance.features.advisor.controller

import com.nivasafinance.TestUtils.createTestAdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.service.AdvisorService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorController Tests")
class AdvisorControllerTest {

    private val advisorService = mockk<AdvisorService>()
    private lateinit var advisorController: AdvisorController

    private val advisorId = UUID.randomUUID()
    private val expectedResponse = createTestAdvisorResponse(id = advisorId)

    @BeforeEach
    fun setup() {
        advisorController = AdvisorController(advisorService)
    }

    @Test
    @DisplayName("should return advisor when found")
    fun `getAdvisor should return advisor when found`() {
        // Given
        every { advisorService.getAdvisor(advisorId) } returns expectedResponse

        // When
        val result = advisorController.getAdvisor(advisorId)

        // Then
        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.advisorCode, result.body?.advisorCode)
        assertEquals(expectedResponse.status, result.body?.status)

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("should create advisor successfully")
    fun `createAdvisor should create advisor successfully`() {
        // Given
        val createRequest = AdvisorCreateRequest(
            advisorCode = "ADV001",
            isEmployee = false,
            remarks = "Test advisor"
        )
        every { advisorService.createAdvisor(createRequest) } returns expectedResponse

        // When
        val result = advisorController.createAdvisor(createRequest)

        // Then
        assertNotNull(result)
        assertEquals(201, result.statusCode.value())
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.advisorCode, result.body?.advisorCode)
        assertEquals(expectedResponse.status, result.body?.status)

        verify(exactly = 1) { advisorService.createAdvisor(createRequest) }
    }

    @Test
    @DisplayName("should update advisor successfully")
    fun `updateAdvisor should update advisor successfully`() {
        // Given
        val updateRequest = AdvisorUpdateRequest(
            advisorCode = "ADV002",
            remarks = "Updated advisor"
        )
        every { advisorService.updateAdvisor(advisorId, updateRequest) } returns expectedResponse
        every { advisorService.getAdvisor(advisorId) } returns expectedResponse

        // When
        val result = advisorController.updateAdvisor(advisorId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedResponse.id, result.body?.id)

        verify(exactly = 1) { advisorService.updateAdvisor(advisorId, updateRequest) }
        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("should delete advisor successfully")
    fun `deleteAdvisor should delete advisor successfully`() {
        // Given
        every { advisorService.deleteAdvisor(advisorId) } returns Unit

        // When
        val result = advisorController.deleteAdvisor(advisorId)

        // Then
        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { advisorService.deleteAdvisor(advisorId) }
    }
}
