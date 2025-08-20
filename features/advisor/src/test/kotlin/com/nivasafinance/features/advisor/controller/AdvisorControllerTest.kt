package com.nivasafinance.features.advisor.controller

import com.nivasafinance.TestUtils.createTestAdvisorDto
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.advisor.service.AdvisorWriteService
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

    private val advisorReadService = mockk<AdvisorReadService>()
    private val advisorWriteService = mockk<AdvisorWriteService>()
    private lateinit var advisorController: AdvisorController

    private val advisorId = UUID.randomUUID()
    private val expectedDto = createTestAdvisorDto(id = advisorId)

    @BeforeEach
    fun setup() {
        advisorController = AdvisorController(advisorReadService, advisorWriteService)
    }

    @Test
    @DisplayName("should return advisor when found")
    fun `getAdvisor should return advisor when found`() {
        // Given
        every { advisorReadService.getAdvisor(advisorId) } returns expectedDto

        // When
        val result = advisorController.getAdvisor(advisorId)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.id, result.body?.id)
        assertEquals(expectedDto.advisorCode, result.body?.advisorCode)
        assertEquals(expectedDto.status, result.body?.status)

        verify(exactly = 1) { advisorReadService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("should create advisor successfully")
    fun `createAdvisor should create advisor successfully`() {
        // Given
        val newAdvisorDto = createTestAdvisorDto(id = null)
        every { advisorWriteService.createAdvisor(newAdvisorDto) } returns expectedDto

        // When
        val result = advisorController.createAdvisor(newAdvisorDto)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.id, result.body?.id)
        assertEquals(expectedDto.advisorCode, result.body?.advisorCode)
        assertEquals(expectedDto.status, result.body?.status)

        verify(exactly = 1) { advisorWriteService.createAdvisor(newAdvisorDto) }
    }

    @Test
    @DisplayName("should update advisor successfully")
    fun `updateAdvisor should update advisor successfully`() {
        // Given
        val updateDto = createTestAdvisorDto(id = advisorId)
        every { advisorWriteService.updateAdvisor(advisorId, updateDto) } returns expectedDto

        // When
        val result = advisorController.updateAdvisor(advisorId, updateDto)

        // Then
        assertNotNull(result)
        assertEquals(200, result.statusCode.value())

        verify(exactly = 1) { advisorWriteService.updateAdvisor(advisorId, updateDto) }
    }

    @Test
    @DisplayName("should delete advisor successfully")
    fun `deleteAdvisor should delete advisor successfully`() {
        // Given
        every { advisorWriteService.deleteAdvisor(advisorId) } returns expectedDto

        // When
        val result = advisorController.deleteAdvisor(advisorId)

        // Then
        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { advisorWriteService.deleteAdvisor(advisorId) }
    }
}
